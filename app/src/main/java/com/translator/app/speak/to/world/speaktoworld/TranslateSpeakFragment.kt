package com.translator.app.speak.to.world.speaktoworld

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentTranslateSpeakBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ViewModelTranslate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList


class TranslateSpeakFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentTranslateSpeakBinding

    var r = 0f

//    fake ad id
    val ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

//    real ad id
//    private val ADMOB_AD_UNIT_ID = "ca-app-pub-5655725305930432/5325170878"
    var currentNativeAd: NativeAd? = null

    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private val SPINNER_FROM_ID: Int = 1
    private val SPINNER_TO_ID: Int = 2

    private val translateViewModel: ViewModelTranslate by activityViewModels()

    private lateinit var languageCode: Array<String>
    private var isOnline: Boolean = false
    var fromLan = "01"
    var toLan = "01"


    var langModelList: ArrayList<String> = ArrayList()

    private val tts: TextToSpeech by lazy {
        TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale(fromLan)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    .let { text -> text?.get(0) }
            binding.editTextFrom.setText(binding.editTextFrom.text.toString() + "$spokenText ")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentTranslateSpeakBinding =
            FragmentTranslateSpeakBinding.inflate(inflater, container, false)
        binding = fragmentTranslateSpeakBinding
        return fragmentTranslateSpeakBinding.root
    }

    @SuppressLint("ServiceCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPrefLang = requireActivity().getSharedPreferences("sharedPrefLang", MODE_PRIVATE)
        fromLan = sharedPrefLang.getString("fromLan", fromLan).toString()
        toLan = sharedPrefLang.getString("toLan", toLan).toString()


        translateViewModel.langModelListSize.observe(viewLifecycleOwner) {
            if (it > 0) {

                /* though below fun is not calling from spinner,
                 it will call the below function multiple times because of livedata observer.
                  so fromSpinner = true is must*/

                translateViewModel.firstPart(fromLan, toLan, binding, fromSpinner = true)
            }
        }

        translateViewModel.initializeTextToSpeech(tts, startForResult)

        languageCode = translateViewModel.languageCode

        with(binding) {

            val spinnerFrom = fromLanguage
            spinnerFrom.id = SPINNER_FROM_ID

            val spinnerTo = toLanguage
            spinnerTo.id = SPINNER_TO_ID

            val adapter1 = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item, translateViewModel.languagesList(false)
            )

            val adapter2 = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item, translateViewModel.languagesList(true)
            )

            spinnerFrom.adapter = adapter1
            spinnerFrom.onItemSelectedListener = this@TranslateSpeakFragment
            spinnerFrom.setSelection(translateViewModel.getSpinnerItemIndex(fromLan))


            spinnerTo.adapter = adapter2
            spinnerTo.onItemSelectedListener = this@TranslateSpeakFragment
            spinnerTo.setSelection(translateViewModel.getSpinnerItemIndex(toLan))

            binding.changeVoice.setOnClickListener {
                changeVoice()
            }
            binding.changeVoiceSpeed.setOnClickListener {
                changeVoiceSpeed()
            }

            swipeSpinner.setOnClickListener {
                hideKeyboard()
                val t0 = fromLan
                val t1 = toLan

                val t2 = editTextFrom.text.toString() + ""
                val t3 = editTextTo.text.toString() + ""
//                editTextTo.setText("")
//                editTextFrom.setText("")
                val pos1 = spinnerFrom.selectedItemPosition
                val pos2 = spinnerTo.selectedItemPosition


                editTextTo.setText(t2)
                editTextFrom.setText(t3)
                fromLan = t1
                toLan = t0

                spinnerFrom.setSelection(pos2)
                spinnerTo.setSelection(pos1)

                runBlocking {
                    val rotate = RotateAnimation(
                        r, r-180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                    )

                    rotate.duration = 500
                    rotate.interpolator = LinearInterpolator()
                    rotate.isFillEnabled = true
                    rotate.fillAfter = true
                    swipeSpinner.startAnimation(rotate)

                    r = if (r == 0f) 180f else 0f
                }

            }

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

                translateViewModel.downloadLanguage(fromLan, toLan, binding, isOnline)
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

                translateViewModel.downloadLanguage(fromLan, toLan, binding, isOnline)
            }

            TranslateFrom.setOnClickListener {

                hideKeyboard()

                if (!checkDemoSelection()) {
                    return@setOnClickListener
                }

                translateViewModel.firstPart(fromLan, toLan, binding)
            }
            clearFrom.setOnClickListener {
                hideKeyboard()
                editTextFrom.setText("")
            }
            copyFrom.setOnClickListener {
                hideKeyboard()
                if (editTextFrom.text.toString() + "" == "") {
                    Snackbar.make(it, "Text Field Empty!", Snackbar.LENGTH_LONG).show()
                }
                val clipBoard =
                    context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("From Text", editTextFrom.text.toString() + "")
                clipBoard.setPrimaryClip(clipData)

                Snackbar.make(it, "Text Copied!", Snackbar.LENGTH_LONG).show()
            }
            speakFrom.setOnClickListener {
                hideKeyboard()
                val text = editTextFrom.text?.trim().toString()
                translateViewModel.speak(
                    text.ifEmpty { "" }, fromLan
                )
            }


//            TranslateTo.setOnClickListener {
//
//                hideKeyboard()
//                if (!checkDemoSelection()) {
//                    return@setOnClickListener
//                }
//
//                translateViewModel.firstPart(fromLan, toLan, binding, false)
//            }


            clearTo.setOnClickListener {
                hideKeyboard()
                editTextTo.setText("")
            }
            copyTo.setOnClickListener {
                hideKeyboard()
                if (editTextTo.text.toString() + "" == "") {
                    Snackbar.make(it, "Text Field Empty!", Snackbar.LENGTH_LONG).show()
                }
                val clipBoard =
                    context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("From Text", editTextTo.text.toString() + "")
                clipBoard.setPrimaryClip(clipData)

                Snackbar.make(it, "Text Copied!", Snackbar.LENGTH_LONG).show()
            }
            speakTo.setOnClickListener {
                hideKeyboard()
                val text = editTextTo.text?.trim().toString()
                translateViewModel.speak(
                    text.ifEmpty { "" }, toLan
                )
            }


            speakButton.setOnClickListener {

                if (!checkDemoSelection()) {
                    return@setOnClickListener
                }

                if (!isOnline(requireContext())) {

                    Snackbar.make(
                        it,
                        "No Internet! \nInternet Required For Voice Recognition",
                        Snackbar.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                permissionGranted = ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permissions[0]
                ) == PackageManager.PERMISSION_GRANTED
                if (!permissionGranted) {
                    ActivityCompat.requestPermissions(requireActivity(), permissions, 200)
                } else translateViewModel.displaySpeechRecognizer(
                    fromLan
                )
            }

            translateViewModel.langModelList.observe(viewLifecycleOwner) {
                langModelList.clear()
                langModelList.addAll(it)
            }

        }


    }

    val TAG = "CameraXApp"

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        isOnline = isOnline(requireContext())

        val sharedPrefLang = requireActivity().getSharedPreferences("sharedPrefLang", MODE_PRIVATE)



        when (p0?.id) {
            1 -> {
                fromLan = languageCode[p2 + 1]
                translateViewModel.modelManager()
                translateViewModel.firstPart(fromLan, toLan, binding, fromSpinner = true)

                sharedPrefLang.edit().apply {
                    putString("fromLan", languageCode[p2])
                }.apply()

            }
            2 -> {
                toLan = languageCode[p2 + 1]
                translateViewModel.modelManager()
                translateViewModel.firstPart(
                    fromLan,
                    toLan,
                    binding,
                    setTextTo = false,
                    fromSpinner = true
                )


                sharedPrefLang.edit().apply {
                    putString("toLan", languageCode[p2])
                }.apply()


            }
        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun checkDemoSelection(): Boolean {
        if (fromLan == "02" || fromLan == "To") {

            Snackbar.make(
                requireView(),
                "Please Select Source (From) Language",
                Snackbar.LENGTH_LONG
            ).show()

            return false
        }

        if (toLan == "02" || toLan == "To") {

            Snackbar.make(
                requireView(),
                "Please Select Target (To) Language",
                Snackbar.LENGTH_LONG
            ).show()

            return false
        }

        if (binding.downloadFromPackage.isVisible || binding.downloadToPackage.isVisible) {

            Snackbar.make(
                requireView(),
                "Please download language model pack.",
                Snackbar.LENGTH_LONG
            ).show()

            return false
        }

        return true
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

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    @SuppressLint("InflateParams")
    private fun refreshAd() {

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()


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
                val adView = layoutInflater
                    .inflate(R.layout.ad_unified, null) as NativeAdView
                populateNativeAdView(nativeAd, adView)
                binding.adFrame.removeAllViews()
                binding.adFrame.addView(adView)
            } catch (_: Exception) {

            }

        }
        builder.withNativeAdOptions(
            NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .setRequestCustomMuteThisAd(true)
//                .setMediaAspectRatio(NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                .build()
        )

        val adLoader = builder.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onStart() {
        super.onStart()
//        initialize ads
        if (isAdded) {
            MobileAds.initialize(requireContext()) {}
            refreshAd()
        }

    }

    fun Fragment.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    private fun changeVoice() {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.google.android.tts") /*replace with the package name of the target TTS engine*/
        try {
            this.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun changeVoiceSpeed() {
        val intent = Intent("com.android.settings.TTS_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            this.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        currentNativeAd?.destroy()
    }

}