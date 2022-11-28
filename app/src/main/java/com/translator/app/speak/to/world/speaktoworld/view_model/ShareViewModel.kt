package com.translator.app.speak.to.world.speaktoworld.view_model

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text

class ShareViewModel : ViewModel() {
    var bitmap1:Bitmap? = null
    var screenWidth:Float = 400f
    var uri: Uri? = null
    var lines: ArrayList<Text.Line> = ArrayList()
    var block: ArrayList<Text.TextBlock> = ArrayList()

    var translatedText = ""
    var theme = 0
}