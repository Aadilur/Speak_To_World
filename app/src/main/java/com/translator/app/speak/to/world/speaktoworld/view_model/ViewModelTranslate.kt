package com.translator.app.speak.to.world.speaktoworld.view_model

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentImageToTextBinding
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentTranslateSpeakBinding
import com.translator.app.speak.to.world.speaktoworld.image_to_text.GraphicOverlay
import com.translator.app.speak.to.world.speaktoworld.image_to_text.TextGraphic
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class ViewModelTranslate : ViewModel() {

    private lateinit var tts: TextToSpeech
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val localCountries = ArrayList<String>()
    private val localCountries2 = ArrayList<String>()

    val TAG = "CameraXApp"


    private var _langModelList = MutableLiveData<ArrayList<String>>()
    val langModelList: LiveData<ArrayList<String>>
        get() = _langModelList


    private var _langModelListSize = MutableLiveData(0)
    val langModelListSize: LiveData<Int>
        get() = _langModelListSize


    init {
        _langModelList.value = ArrayList<String>()
        modelManager()
    }

    fun initializeTextToSpeech(
        engine: TextToSpeech,
        launcher: ActivityResultLauncher<Intent>
    ) = viewModelScope.launch {
        tts = engine
        startForResult = launcher

    }


    fun modelManager() {

        val modelManager = RemoteModelManager.getInstance()
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                Log.d("datata", models.size.toString())
                _langModelList.value?.clear()
                for (i in models.toList()) {
                    _langModelList.value?.add(i.language)
                }
                _langModelListSize.value = _langModelList.value?.size
            }.addOnFailureListener {}
    }

    @SuppressLint("SetTextI18n")
    fun firstPart(
        fromLan: String,
        toLan: String,
        binding: FragmentTranslateSpeakBinding,
        setTextTo: Boolean = true,
        fromSpinner: Boolean = false
    ) {


        binding.downloadFromPackage.visibility = View.GONE
        binding.downloadToPackage.visibility = View.GONE


        if (_langModelList.value!!.contains(fromLan.take(2))
            && _langModelList.value!!.contains(toLan.take(2))
        ) {

            if (fromSpinner) return

            if (setTextTo) {

                Log.d(TAG, "onItemSelected: 1111")
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(fromLan.take(2))
                    .setTargetLanguage(toLan.take(2))
                    .build()
                val translateText = Translation.getClient(options)

                Log.d(TAG, "onItemSelected: toLan editText")
                binding.editTextTo.setText("")
                val textData = binding.editTextFrom.text.toString().split("\n").toTypedArray()

                for (data in textData) {
                    translateText.translate(data)
                        .addOnSuccessListener {
//                            binding.editTextTo.append(it + "\n")

                            if (textData[textData.lastIndex] == data) binding.editTextTo.append(it)
                            else binding.editTextTo.append(it + "\n")
                        }.addOnFailureListener {}
                }


            } else {

                Log.d(TAG, "onItemSelected: 2222")

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(toLan.take(2))
                    .setTargetLanguage(fromLan.take(2))
                    .build()
                val translateText = Translation.getClient(options)


                binding.editTextFrom.setText("")
                val textData = binding.editTextTo.text.toString().split("\n").toTypedArray()

                for (data in textData) {

                    translateText.translate(data)
                        .addOnSuccessListener {

                            if (textData[textData.lastIndex] == data) binding.editTextFrom.append(it)
                            else binding.editTextFrom.append(it + "\n")

                        }.addOnFailureListener {}
                }
            }

        } else {
            Log.d(TAG, "_langModelList: ${_langModelList.value!!.size}")
            if (!_langModelList.value!!.contains(fromLan.take(2)) && fromLan != "02" && _langModelList.value!!.size > 0) {
                binding.downloadFromPackage.visibility = View.VISIBLE
            }

            if (!_langModelList.value!!.contains(toLan.take(2)) && toLan != "02" && _langModelList.value!!.size > 0) {
                binding.downloadToPackage.visibility = View.VISIBLE
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun firstPartImage(
        fromLan: String,
        toLan: String,
        binding: FragmentImageToTextBinding,
        setTextTo: Boolean = true,
        fromSpinner: Boolean = false
    ) {


        binding.downloadFromPackage.visibility = View.GONE
        binding.downloadToPackage.visibility = View.GONE


        if (_langModelList.value!!.contains(fromLan.take(2))
            && _langModelList.value!!.contains(toLan.take(2))
        ) {

            if (fromSpinner) return

            if (setTextTo) {

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(fromLan.take(2))
                    .setTargetLanguage(toLan.take(2))
                    .build()
                val translateText = Translation.getClient(options)


                val data = binding.filteredTest.text.toString().replace("[\r\n]+", "\n") + ""

                translateText.translate(data)
                    .addOnSuccessListener {
                        translateText.close()
                    }.addOnFailureListener {}

            }

        } else {
            if (!_langModelList.value!!.contains(fromLan.take(2)) && fromLan != "02" && _langModelList.value!!.size > 0) {
                binding.downloadFromPackage.visibility = View.VISIBLE
            }

            if (!_langModelList.value!!.contains(toLan.take(2)) && toLan != "02" && _langModelList.value!!.size > 0) {
                binding.downloadToPackage.visibility = View.VISIBLE
            }
        }

    }
var xx = 0
    fun fromImageToText(
        fromLan: String,
        toLan: String,
        binding: FragmentImageToTextBinding,
        shareViewModel: ShareViewModel,
        mGraphicOverlay: GraphicOverlay,
        context: Context
    ) {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLan.take(2))
            .setTargetLanguage(toLan.take(2))
            .build()
        val translateText = Translation.getClient(options)

        if (shareViewModel.block.size > 0) {
            mGraphicOverlay.clear()

            for (data in shareViewModel.block) {

                Log.d(TAG, "fromImageToText: ${xx++}")

                translateText.translate(data.text)
                    .addOnSuccessListener {

                        val textSize = data.lines[0].boundingBox?.width()
                            ?.let { it1 -> data.lines[0].boundingBox?.height()
                                ?.let { it2 -> min( it2.toInt(), it1.toInt()) } }

                        val textGraphic = TextGraphic(
                            mGraphicOverlay, data, it, context, textSize
                        )

                        mGraphicOverlay.add(textGraphic)

//                        val bitmap: Bitmap = getBitmapFromView(binding.constraintLayout)
//                        binding.imageView2.setImageBitmap(bitmap)

                    }.addOnFailureListener {}
            }


        }

    }


//    fun fromImageToText(
//        fromLan: String,
//        toLan: String,
//        binding: FragmentImageToTextBinding,
//        shareViewModel: ShareViewModel,
//        mGraphicOverlay: GraphicOverlay,
//        context: Context
//    ) {
//
//        val options = TranslatorOptions.Builder()
//            .setSourceLanguage(fromLan.take(2))
//            .setTargetLanguage(toLan.take(2))
//            .build()
//        val translateText = Translation.getClient(options)
//
//
//        if (shareViewModel.lines.size > 0) {
//            mGraphicOverlay.clear()
//
//            for (data in shareViewModel.lines) {
//
//                translateText.translate(data.text)
//                    .addOnSuccessListener {
//
//                        val textGraphic = TextGraphic(
//                            mGraphicOverlay, data, it, context
//                        )
//                        mGraphicOverlay.add(textGraphic)
//                        val bitmap: Bitmap = getBitmapFromView(binding.constraintLayout)
//                        binding.imageView2.setImageBitmap(bitmap)
//
//                    }.addOnFailureListener {}
//            }
//
//
//        }
//
//    }



    fun imageTranslateText(
        fromLan: String,
        toLan: String,
        binding: FragmentImageToTextBinding,
        fromSpinner: Boolean = false
    ) {
        if (fromSpinner)return
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLan.take(2))
            .setTargetLanguage(toLan.take(2))
            .build()
        val translateText = Translation.getClient(options)

        val t1 = binding.filteredTest.text.toString() + ""

        binding.TranslatedText.text = ""
        if (t1 != "") {
            val t2 = t1.split("\n").toTypedArray()
            for (data in t2) {
                translateText.translate(data)
                    .addOnSuccessListener {
                        binding.TranslatedText.append(it + "\n")
                    }.addOnFailureListener {}
            }


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


    @OptIn(DelicateCoroutinesApi::class)
    fun downloadLanguage(
        fromLan: String,
        toLan: String,
        binding: FragmentTranslateSpeakBinding,
        boolean: Boolean = true,
    ) {
        if (!boolean) {
            return
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLan.take(2))
            .setTargetLanguage(toLan.take(2))
            .build()
        val translateText = Translation.getClient(options)


        val conditions = DownloadConditions.Builder()
            .build()

        translateText.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {

                modelManager()

                binding.downloadingText.visibility = View.GONE

            }
            .addOnFailureListener {

                binding.downloadingText.visibility = View.GONE
            }

    }


    fun downloadLanguageImage(
        fromLan: String,
        toLan: String,
        binding: FragmentImageToTextBinding,
        boolean: Boolean = true,
    ) {
        if (!boolean) {
            return
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(fromLan.take(2))
            .setTargetLanguage(toLan.take(2))
            .build()
        val translateText = Translation.getClient(options)


        val conditions = DownloadConditions.Builder()
            .build()

        translateText.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                modelManager()
                binding.downloadingText.visibility = View.GONE
            }
            .addOnFailureListener {
                binding.downloadingText.visibility = View.GONE
            }

    }


    fun speak(text: String, languageCode: String) = viewModelScope.launch {

        tts.language = Locale(
            languageCode.take(2),
            (if (languageCode.length > 2) languageCode.takeLast(2) else "") as String
        )
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

    }

    fun languageName(s:String):String?{
        val languageName = mapLanguageList[s]
        return languageName
    }

    fun languagesList(toLan: Boolean = false): ArrayList<String> {

        if (toLan) {
            localCountries.clear()
            for (l in languageCode) {
                val a = mapLanguageList[l] + ""
                localCountries.add(a)
            }
            localCountries.sorted()
            localCountries.removeAt(0)
            return localCountries

        } else {
            localCountries2.clear()
            for (l in languageCode) {
                val a = mapLanguageList[l] + ""
                localCountries2.add(a)
            }
            localCountries2.sorted()
            localCountries2.removeAt(1)
            return localCountries2
        }

    }

    fun getSpinnerItemIndex(myString: String): Int {
        return languageCode.indexOf(myString)
    }

    fun displaySpeechRecognizer(fromLan: String) {
        try {
            startForResult.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, fromLan.take(2))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, fromLan.take(2))
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, fromLan.take(2))
            })
        } catch (e: Exception) {
        }

    }


    var languageCode: Array<String> = arrayOf(
        "01",
        "02",
        "af",
        "sq",
        "ar",
        "be",
        "bg",
        "bn",
        "bnIN",
        "ca",
        "zh",
        "zhHK",
        "zhTW",
        "hr",
        "cs",
        "da",
        "nl",
        "enAU",
        "enGB",
        "en",
        "eo",
        "et",
        "fi",
        "fr",
        "gl",
        "ka",
        "de",
        "el",
        "gu",
        "ht",
        "hi",
        "hu",
        "is",
        "id",
        "ga",
        "it",
        "ja",
        "kn",
        "ko",
        "lt",
        "lv",
        "mk",
        "mr",
        "ms",
        "mt",
        "no",
        "fa",
        "pl",
        "pt",
        "ro",
        "ru",
        "sk",
        "sl",
        "es",
        "sv",
        "sw",
        "tl",
        "ta",
        "te",
        "th",
        "tr",
        "uk",
        "ur",
        "vi",
        "cy"
    )


    private var mapLanguageList = hashMapOf(
        Pair("01", "From"),
        Pair("02", "To"),
        Pair("af", "Afrikaans (Afrikaans)"),
        Pair("sq", "Albanian (shqip)"),
        Pair("ar", "Arabic (العربية)"),
        Pair("be", "Belarusian (беларуская)"),
        Pair("bg", "Bulgarian (български)"),
        Pair("bn", "Bengali (বাংলা) BD"),
        Pair("bnIN", "Bengali (বাংলা) IN"),
        Pair("ca", "Catalan (català)"),
        Pair("zh", "Chinese CN (中文)"),
        Pair("zhHK", "Chinese HK (中文)"),
        Pair("zhTW", "Chinese TW (中文)"),
        Pair("hr", "Croatian (hrvatski)"),
        Pair("cs", "Czech (čeština)"),
        Pair("da", "Danish (dansk)"),
        Pair("nl", "Dutch (Nederlands)"),
        Pair("enAU", "English (AU)"),
        Pair("enGB", "English (GB)"),
        Pair("en", "English (US)"),
        Pair("eo", "Esperanto (esperanto)"),
        Pair("et", "Estonian (eesti)"),
        Pair("fi", "Finnish (suomi)"),
        Pair("fr", "French (français)"),
        Pair("gl", "Galician (galego)"),
        Pair("ka", "Georgian (ქართული)"),
        Pair("de", "German (Deutsch)"),
        Pair("el", "Greek (Ελληνικά)"),
        Pair("gu", "Gujarati (ગુજરાતી)"),
        Pair("ht", "Haitian (Haitian Creole)"),
        Pair("hi", "Hindi (हिन्दी)"),
        Pair("hu", "Hungarian (magyar)"),
        Pair("is", "Icelandic (íslenska)"),
        Pair("id", "Indonesian (Indonesia)"),
        Pair("ga", "Irish (Gaeilge)"),
        Pair("it", "Italian (italiano)"),
        Pair("ja", "Japanese (日本語)"),
        Pair("kn", "Kannada (ಕನ್ನಡ)"),
        Pair("ko", "Korean (한국어)"),
        Pair("lt", "Lithuanian (lietuvių)"),
        Pair("lv", "Latvian (latviešu)"),
        Pair("mk", "Macedonian (македонски)"),
        Pair("mr", "Marathi (मराठी)"),
        Pair("ms", "Melayu (Malay)"),
        Pair("mt", "Maltese (Malti)"),
        Pair("no", "Norwegian (norsk)"),
        Pair("fa", "Persian (فارسی)"),
        Pair("pl", "Polish (polski)"),
        Pair("pt", "Portuguese (português)"),
        Pair("ro", "Romanian (română)"),
        Pair("ru", "Russian (русский)"),
        Pair("sk", "Slovak (slovenčina)"),
        Pair("sl", "Slovenian (slovenščina)"),
        Pair("es", "Spanish (español)"),
        Pair("sv", "Swedish (svenska)"),
        Pair("sw", "Swahili (Kiswahili)"),
        Pair("tl", "Tagalog (Tagalog)"),
        Pair("ta", "Tamil (தமிழ்)"),
        Pair("te", "Telugu (తెలుగు)"),
        Pair("th", "Thai (ไทย)"),
        Pair("tr", "Turkish (Türkçe)"),
        Pair("uk", "Ukrainian (українська)"),
        Pair("ur", "Urdu (اردو)"),
        Pair("vi", "Vietnamese (Tiếng Việt)"),
        Pair("cy", "Welsh (Cymraeg)"),
    )

}

