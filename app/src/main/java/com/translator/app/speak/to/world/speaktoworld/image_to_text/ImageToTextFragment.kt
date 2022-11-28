package com.translator.app.speak.to.world.speaktoworld.image_to_text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentImageToTextBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel
import com.translator.app.speak.to.world.speaktoworld.view_model.ViewModelTranslate
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.time.Duration


class ImageToTextFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        var bitmapCompanion: Bitmap? = null
    }

    lateinit var binding: FragmentImageToTextBinding
    val TAG = "CameraXApp"
    var btnPosition = 0
//    fake ad id
        val ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    //    real ad id
//    private val ADMOB_AD_UNIT_ID = "ca-app-pub-5655725305930432/4525140811"
    var currentNativeAd: NativeAd? = null


    private val shareViewModel: ShareViewModel by activityViewModels()
    private val translateViewModel: ViewModelTranslate by activityViewModels()

    private lateinit var mGraphicOverlay: GraphicOverlay

    private var cameraPermissionGranted = false


    private var cameraPermissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )


    private val SPINNER_FROM_ID: Int = 1
    private val SPINNER_TO_ID: Int = 2

    private lateinit var languageCode: Array<String>
    private var isOnline: Boolean = false
    var fromLan = "01"
    var toLan = "01"

    lateinit var spinnerFrom: Spinner
    lateinit var spinnerTo: Spinner
    lateinit var adapter1: SpinnerAdapter
    lateinit var adapter2: SpinnerAdapter

    var langModelList: ArrayList<String> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentImageToTextBinding =
            FragmentImageToTextBinding.inflate(inflater, container, false)
        binding = fragmentImageToTextBinding
        return fragmentImageToTextBinding.root
    }


    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        runBlocking {

            translateViewModel.langModelListSize.observe(viewLifecycleOwner) {
                if (it > 0) {

                    /* though below fun is not calling from spinner,
                     it will call the below function multiple times because of livedata observer.
                      so fromSpinner = true is must*/

                    translateViewModel.firstPartImage(fromLan, toLan, binding, fromSpinner = true)
                }
            }

            with(binding) {


                binding.TranslateFrom?.text = "Translated From: ${translateViewModel.languageName(fromLan)} "
                binding.TranslateTo?.text  = "Translated To: ${translateViewModel.languageName(toLan)} "

                val sharedPrefLangImg = requireActivity().getSharedPreferences("sharedPrefLang",
                    Context.MODE_PRIVATE
                )
                fromLan = sharedPrefLangImg.getString("fromLan", fromLan).toString()
                toLan = sharedPrefLangImg.getString("toLan", toLan).toString()

                if (bitmapCompanion != null) imageView.setImageBitmap(bitmapCompanion)

                mGraphicOverlay = graficOverlay


                languageCode = translateViewModel.languageCode

                spinnerFrom = fromLanguage
                spinnerFrom.id = SPINNER_FROM_ID

                spinnerTo = toLanguage
                spinnerTo.id = SPINNER_TO_ID

                adapter1 = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item, translateViewModel.languagesList(false)
                )

                adapter2 = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item, translateViewModel.languagesList(true)
                )

                spinnerFrom.adapter = adapter1
                spinnerFrom.onItemSelectedListener = this@ImageToTextFragment
                spinnerFrom.setSelection(translateViewModel.getSpinnerItemIndex(fromLan))



                spinnerTo.adapter = adapter2
                spinnerTo.onItemSelectedListener = this@ImageToTextFragment
                spinnerTo.setSelection(translateViewModel.getSpinnerItemIndex(toLan))



                downloadFromPackage.setOnClickListener {
                    if (!isOnline(requireContext())) {

                        Snackbar.make(
                            it,
                            "No Internet...!!! \n" +
                                    "Internet Connection Required For Download This Package",
                            Snackbar.LENGTH_LONG
                        ).show()

                        return@setOnClickListener
                    }
                    downloadingText.visibility = View.VISIBLE
                    downloadFromPackage.visibility = View.GONE
                    downloadToPackage.visibility = View.GONE

                    translateViewModel.downloadLanguageImage(fromLan, toLan, binding, isOnline)
                }

                downloadToPackage.setOnClickListener {
                    if (!isOnline(requireContext())) {

                        Snackbar.make(
                            it,
                            "No Internet...!!! \n" +
                                    "Internet Connection Required For Download This Package",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                    downloadingText.visibility = View.VISIBLE
                    downloadFromPackage.visibility = View.GONE
                    downloadToPackage.visibility = View.GONE

                    translateViewModel.downloadLanguageImage(fromLan, toLan, binding, isOnline)
                }


                devanagari.setOnClickListener {
                    btnPosition = 0
                    identifyDefLang()

                    bmc(true)
                    translateViewModel.modelManager()
                    translateViewModel.firstPartImage(fromLan, toLan, binding, fromSpinner = true)
                    translateViewModel.imageTranslateText(fromLan, toLan, binding)

                }
                chinese.setOnClickListener {
                    selectFrom("zh")
                    btnPosition = 1
                    bmc()
                }
                korean.setOnClickListener {
                    selectFrom("ko")
                    btnPosition = 2
                    bmc()

                }
                japanese.setOnClickListener {
                    selectFrom("ja")
                    btnPosition = 3
                    bmc()
                }


                languageIdentity.observe(viewLifecycleOwner) {
                    if (it == "") {
                        return@observe
                    } else {
                        spinnerFrom.setSelection(
                            translateViewModel.getSpinnerItemIndex(
                                it
                            ) - 1
                        )

                        if (it == "cn") toggleButtonGroup.check(R.id.chinese)
                        if (it == "ko") toggleButtonGroup.check(R.id.korean)
                        if (it == "ja") toggleButtonGroup.check(R.id.japanese)


                    }
                }


//            captureImage.setOnClickListener {
//                findNavController().navigate(R.id.action_imageToTextFragment2_to_captureImage2)
//            }

                pickImage.setOnClickListener {
                    findNavController().navigate(R.id.action_imageToTextFragment2_to_pickImage2)
                }

                captureImage.setOnClickListener {

                    findNavController().navigate(R.id.action_imageToTextFragment2_to_captureImage2)

                }

                createZoomableImg.setOnClickListener {

                    if (constraintLayout.height <1 ) {
                        Snackbar.make(requireView(), "No Image Found..!", Snackbar.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val bitmap: Bitmap = getBitmapFromView(binding.constraintLayout)
                    binding.imageView2.setImageBitmap(bitmap)

                    binding.imageView2.visibility = View.VISIBLE
                    createZoomableImg.isEnabled = false
                    createZoomableImg.setBackgroundColor(resources.getColor(R.color.themeColorDisable))
                    createZoomableImg.text = "Try to zoom the Image!"
                }


            }


            // init ads in onViewCreated

            MobileAds.initialize(requireContext()) {}
            refreshAd()
        }

    }


    var languageIdentity: MutableLiveData<String> = MutableLiveData("")

    override fun onResume() {
        super.onResume()

        runBlocking {

            with(binding) {
                if (shareViewModel.bitmap1 != null) {

                    bitmapCompanion = shareViewModel.bitmap1

                    if (bitmapCompanion != null) {

                        imageView.setImageBitmap(bitmapCompanion)
                        createZoomableImg.visibility = View.VISIBLE

                        // making the image overlay height same as imageView
                        graficOverlay.layoutParams.height = bitmapCompanion!!.height
                        graficOverlay.requestLayout()

                        var recognizer =
                            TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())


                        if (btnPosition == 1) recognizer = TextRecognition.getClient(
                            ChineseTextRecognizerOptions.Builder().build()
                        )

                        if (btnPosition == 2) recognizer = TextRecognition.getClient(
                            KoreanTextRecognizerOptions.Builder().build()
                        )

                        if (btnPosition == 3) recognizer = TextRecognition.getClient(
                            JapaneseTextRecognizerOptions.Builder().build()
                        )


                        val result = bitmapCompanion?.let { result ->
                            recognizer.process(result, 0).addOnSuccessListener {
                                filteredTest.text = it.text

                                val languageIdentifier = LanguageIdentification.getClient()
                                languageIdentifier.identifyLanguage(it.text)
                                    .addOnSuccessListener { languageCode ->
                                        if (languageCode == "und") {
                                            Log.i(TAG, "Can't identify language.")
                                            languageIdentity.value = ""
                                        } else {
                                            Log.i(TAG, "Language: $languageCode")
                                            languageIdentity.value = languageCode
                                        }
                                    }.addOnFailureListener {}


                                translateViewModel.imageTranslateText(fromLan, toLan, binding)


                            }
                        }


                    }

                }

            }
        }



    }

    fun identifyDefLang(){
        val a = binding.filteredTest.text.toString()

        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(a)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.i(TAG, "Can't identify language.")
                    languageIdentity.value = ""
                } else {
                    Log.i(TAG, "Language: $languageCode")
                    languageIdentity.value = languageCode
                }
            }.addOnFailureListener {}
    }

    fun selectFrom(s: String) {
        spinnerFrom.setSelection(
            translateViewModel.getSpinnerItemIndex(
                s
            ) - 1
        )
    }

    fun bmc(spinner:Boolean = false) {

        var recognizer =
            TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())


        if (btnPosition == 1) recognizer = TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build()
        )

        if (btnPosition == 2) recognizer = TextRecognition.getClient(
            KoreanTextRecognizerOptions.Builder().build()
        )

        if (btnPosition == 3) recognizer = TextRecognition.getClient(
            JapaneseTextRecognizerOptions.Builder().build()
        )

        val result = bitmapCompanion?.let { result ->
            recognizer.process(result, 0).addOnSuccessListener {
                binding.filteredTest.text = it.text


                translateViewModel.imageTranslateText(fromLan, toLan, binding, fromSpinner = spinner)

                processTextRecognitionResult(it)
                binding.imageView2.visibility = View.GONE
                binding.createZoomableImg.text = "Create Zoomable Image"
                binding.createZoomableImg.isEnabled = true

            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        isOnline = isOnline(requireContext())

        val sharedPrefLangImg = requireActivity().getSharedPreferences("sharedPrefLang",
            Context.MODE_PRIVATE
        )

        if (view != null) {
            when (p0?.id) {
                1 -> {
                    bmc(true)
                    fromLan = languageCode[p2 + 1]
                    translateViewModel.modelManager()
                    translateViewModel.firstPartImage(fromLan, toLan, binding, fromSpinner = true)

                    binding.TranslateFrom?.text = "Translated From: ${translateViewModel.languageName(fromLan)} "
                    sharedPrefLangImg.edit().apply {
                        putString("fromLan", languageCode[p2])
                    }.apply()

                }
                2 -> {
                    bmc(true)
                    toLan = languageCode[p2 + 1]
                    translateViewModel.modelManager()
                    translateViewModel.firstPartImage(fromLan, toLan, binding, fromSpinner = true)
                    translateViewModel.imageTranslateText(fromLan, toLan, binding)

                    binding.TranslateTo?.text  = "Translated To: ${translateViewModel.languageName(toLan)}  "
                    sharedPrefLangImg.edit().apply {
                        putString("toLan", languageCode[p2])
                    }.apply()
                }
            }
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }


    private fun isOnline(requireContext: Context): Boolean {

        val connectivityManager =
            requireContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                //It will check for both wifi and cellular network
                return networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }

    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {

            return
        }
        mGraphicOverlay.clear()
        shareViewModel.lines = ArrayList()
        shareViewModel.lines.clear()
        shareViewModel.block.clear()
        val a: ArrayList<Text.TextBlock> = ArrayList()
        for (i in blocks.indices) {
                a.add(blocks[i])
        }

        shareViewModel.block.addAll(a)

        if (shareViewModel.block.size > 0) {
            try {
                translateViewModel.fromImageToText(
                    fromLan,
                    toLan,
                    binding,
                    shareViewModel,
                    mGraphicOverlay,
                    requireContext()
                )


            } catch (e: Exception) {
                Log.d(TAG, "onResume: $e")
            }
        }
    }


//    private fun processTextRecognitionResult(texts: Text) {
//        val blocks = texts.textBlocks
//        if (blocks.size == 0) {
//
//            return
//        }
//        mGraphicOverlay.clear()
//        shareViewModel.lines = ArrayList()
//        shareViewModel.lines.clear()
//        val a: ArrayList<Text.Line> = ArrayList()
//        for (i in blocks.indices) {
//            val lines = blocks[i].lines
//            for (j in lines.indices) {
//                a.add(lines[j])
//            }
//        }
//
//        shareViewModel.lines.addAll(a)
//
//        if (shareViewModel.lines.size > 0) {
//            try {
//                translateViewModel.fromImageToText(
//                    fromLan,
//                    toLan,
//                    binding,
//                    shareViewModel,
//                    mGraphicOverlay,
//                    requireContext()
//                )
//
//
//            } catch (e: Exception) {
//                Log.d(TAG, "onResume: $e")
//            }
//        }
//    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    // ------------------------------------ ads ------------------------------------------------

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
//        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)


        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
//        adView.mediaView!!.setMediaContent(nativeAd.mediaContent!!)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.GONE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.GONE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }


//        adView.mediaView!!.visibility = View.VISIBLE
//        adView.mediaView!!.setMediaContent(nativeAd.mediaContent!!)

        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE

        } else {

            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon!!.drawable
            )
            adView.iconView!!.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView!!.visibility = View.GONE
        } else {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView!!.visibility = View.GONE
        } else {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.GONE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)



    }


    @SuppressLint("InflateParams")
    private fun refreshAd() {

        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()


        val builder = AdLoader.Builder(requireContext(), ADMOB_AD_UNIT_ID)

        builder.forNativeAd { nativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            var activityDestroyed = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activityDestroyed = isDetached
            }
            if (activityDestroyed || isRemoving) {
                nativeAd.destroy()
                return@forNativeAd
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            currentNativeAd?.destroy()

            currentNativeAd = nativeAd


            try {
                val adView = layoutInflater.inflate(R.layout.ad_unified, null) as NativeAdView
                populateNativeAdView(nativeAd, adView)
                binding.adFrame.removeAllViews()
                binding.adFrame.addView(adView)
            } catch (e: Exception) {

            }
        }
        builder.withNativeAdOptions(
            NativeAdOptions.Builder().setVideoOptions(videoOptions).setRequestCustomMuteThisAd(true)
                .build()
        )

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

}