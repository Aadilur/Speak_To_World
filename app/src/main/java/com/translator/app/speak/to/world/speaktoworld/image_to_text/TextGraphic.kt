// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.translator.app.speak.to.world.speaktoworld.image_to_text

import android.content.Context
import android.graphics.*
import android.util.Log
import com.google.mlkit.vision.text.Text
import com.translator.app.speak.to.world.speaktoworld.R
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphic internal constructor(
    overlay: GraphicOverlay?,
    element: Text.TextBlock,
    stringData: String,
    context: Context,
    textSize: Int? = 10
) :
    GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint
    private val textPaint: Paint
    private val element: Text.TextBlock?
    private var text: String = stringData

    private val rectColor = context.resources?.getColor(R.color.custom5, null)

    private var textSize: Float = 0.0f
    private var numOfChars: Int = 0
    private var maxLine: Int = 1


    init {
        this.element = element
        if (textSize != null) {
            this.textSize = textSize * 0.7f
        }
        rectPaint = Paint()
        if (rectColor != null) {
            rectPaint.color = rectColor
        }else rectPaint.color = Color.GRAY
        rectPaint.style = Paint.Style.FILL
        //        rectPaint.setStrokeWidth(STROKE_WIDTH);
        textPaint = Paint()
        textPaint.color = TEXT_COLOR
        textPaint.isAntiAlias=true
        if (textSize != null) {
            textPaint.textSize = textSize * 0.7f
        }

        numOfChars =
            element.boundingBox?.width()
                ?.let { textPaint.breakText(text, true, it.toFloat(), null) }!!


        maxLine = ceil(text.length.toDouble() / numOfChars).toInt()


//        textPaint.textSize = if (element.lines[0].boundingBox?.height()!! > element.lines[0].boundingBox?.width()!!) (element.lines[0].boundingBox?.width()!!) * 0.7f else (element.lines[0].boundingBox?.height()!!) * 0.7f


        // Redraw the overlay, as this graphic has been added.
        postInvalidate()


    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas?) {
//        Log.d(TAG, "on draw text graphic")
        checkNotNull(element) { "Attempting to draw a null text." }
//        Log.d(TAG, "draw: $maxLine ------ ${text.length} --- $text--- $numOfChars")

        val start = (text.length - numOfChars) / 2

        // Draws the bounding box around the TextBlock.
        val rect = RectF(element.boundingBox)
        canvas?.drawRect(rect, rectPaint)

        // Renders the text at the bottom of the box.
//        canvas?.drawText(text, rect.left, rect.centerY()+textPaint.textSize/2.5f, textPaint)
//        canvas?.drawText(text, rect.left, rect.centerY(), textPaint)
        var x = 0
        var y = 0f
        var z = numOfChars
        for (i in 1..maxLine){
            try {

                canvas?.drawText(text,x,z,rect.left,rect.top+textSize+y,textPaint)
            }catch (e:Exception){
                Log.e(TAG, "draw: $e", )
            }

            x+= numOfChars

            z += if (numOfChars>(text.length-numOfChars)){
                text.length-numOfChars
            }else numOfChars

            y+=textSize
        }
    }

    companion object {
        //    int a = getApplicationContext().getColor(R.color.custom3);
        private const val TAG = "TextGraphic"
        private const val TEXT_COLOR = Color.RED
        private const val TEXT_SIZE = 40.0f
        private const val STROKE_WIDTH = 4.0f
    }

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
     * to view coordinates for the graphics that are drawn:
     *
     *
     *
     *  1. [Graphic.scaleX] and [Graphic.scaleY] adjust the size of the
     * supplied value from the preview scale to the view scale.
     *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
     * coordinate from the preview's coordinate system to the view coordinate system.
     *
     *
     * @param canvas drawing canvas
     */

}