package com.translator.app.speak.to.world.speaktoworld.util


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Selection.moveDown
import android.text.Selection.moveUp
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.translator.app.speak.to.world.speaktoworld.R
import kotlin.math.max


class CV4 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {




//    onMeasure will be called before onSizeChanged and onDraw
//    please see the View lifecycle for more details

    // pass bitmap image

    val TAG = "CustomView"

    var src = BitmapFactory.decodeResource(resources, R.drawable.a2)
    var aspectRatio = src.width / src.height.toFloat()
    private var screenWidth = 0
    private var imageWidth = 0
    private var imageHeight = 0


    //get the bitmap from test():Bitmap? function
    private lateinit var output: Bitmap

    val attributeSet = attrs

    fun test( bitmap: Bitmap?,a: Int =0): Bitmap? {

        src = bitmap
        aspectRatio = src.width/src.height.toFloat()

        val cropWidth: Float = (pr - pl)
        val cropHeight: Float = (pb - pt)

        //returning the bitmap
        requestLayout()
//        output = Bitmap.createScaledBitmap(src, src.width, src.height, true)

        if (a==1) return Bitmap.createBitmap(
            output,
            pl.toInt(),
            pt.toInt(),
            cropWidth.toInt(),
            cropHeight.toInt()
        )

        return null
    }


    private var motionX = 0f
    private var motionY = 0f
    private var rng_v = 40f // Touch range for the 4 side and 4 corners of the rect
    private var rng_h = 40f // Touch range for the 4 side and 4 corners of the rect

    // p for point and l=left t=top r=right b=bottom
    private var pl = 100f
    private var plx = 100f //hold motionX value from moveDown() function

    private var pt = 100f
    private var pty = 100f //hold motionY value from moveDown() function

    private var pr = 300f
    private var pb = 400f

    //hold left,top,right,bottom value from moveDown() function
    private var l = 0f
    private var t = 0f
    private var r = 0f
    private var b = 0f


    // check user touch Down on rect edges, corners or inside

    //edges point
    private var c1 = false
    private var c2 = false
    private var c3 = false
    private var c4 = false

    private var c5 = false  // for inside selection to move the whole rect

    //corners point
    private var c6 = false
    private var c7 = false
    private var c8 = false
    private var c9 = false


    // resizable rect
    private var rectF = RectF(pl + 5, pt + 5, pr - 5, pb - 5)


    private val rectPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.YELLOW

    }


    // dark overlay rect
    private val foregroundArcColor =
        context.resources?.getColor(R.color.custom3, null) ?: Color.GRAY
    private var rectF2 = RectF(0f, 0f, screenWidth.toFloat(), imageHeight.toFloat())
    private val rectPaint2 = Paint().apply {
        style = Paint.Style.FILL
        color = foregroundArcColor
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (w/3f)*4
        val r = w/h.toFloat()

        if (r<=aspectRatio){
            screenWidth = w
            imageWidth = screenWidth
            imageHeight = ((screenWidth.toFloat() / src.width) * src.height).toInt()
            setMeasuredDimension(screenWidth, imageHeight)
        }else {

            imageHeight = h.toInt()
            screenWidth = ((imageHeight.toFloat()/src.height)*src.width).toInt()
            imageWidth = screenWidth
            setMeasuredDimension(screenWidth, imageHeight)
        }



    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val desiredWidthInPx = imageWidth
        val derivedHeightInPx = (desiredWidthInPx / aspectRatio).toInt()

        output = Bitmap.createScaledBitmap(src, desiredWidthInPx, derivedHeightInPx, true)
        rectF2 = RectF(0f, 0f, imageWidth.toFloat(), derivedHeightInPx.toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        parent.requestDisallowInterceptTouchEvent(true)
        canvas?.apply {

            // background image under overlay
            drawBitmap(output, 0f, 0f, null)

            // dark overlay
            drawRect(rectF2, rectPaint2)

            // clip rect same as movable rect. This will hide everything outside
            clipRect(rectF)

            // visible clear image covered by clip rect
            drawBitmap(output, 0f, 0f, null)

            // movable rect
            drawRoundRect(rectF, 10f, 10f, rectPaint)
        }
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            motionX = event.x
            motionY = event.y

            if (motionX < 0) motionX = 0f
            if (motionX > screenWidth) motionX = screenWidth.toFloat()
            if (motionY < 0) motionY = 0f
            if ( motionY > imageHeight) motionY = imageHeight.toFloat()
            rng_h = max(40f,(pr-pl)*0.1f)
            rng_v =max(40f, (pb-pt)*0.1f)


            when (event.action) {
                MotionEvent.ACTION_MOVE -> moveMove()
                MotionEvent.ACTION_DOWN -> moveDown()
                MotionEvent.ACTION_UP -> moveUp()
            }
        }

        return true

    }

    var a1 = 0
    var b1 = 0

    private fun moveMove() {

        //moving the whole rect
        if (c5) {


            if (motionX-(plx-l)>0 ) {
                pl = motionX - (plx - l)
                if (pl>screenWidth-100f) pl = screenWidth-110f
                Log.d(TAG, "moveMove: left")
            } else {
                pl = 0f
                pr = r-l
                Log.d(TAG, "moveMove: left 2")
            }

            if (motionX+r-plx<screenWidth) {
                pr = motionX + r - plx
                if (pr<100f) pr = 110f
                Log.d(TAG, "moveMove: right")
            } else {
                pr = screenWidth.toFloat()
                Log.d(TAG, "moveMove: right 2")
            }

            if (motionY-(pty-t)>0 ) {
                pt = motionY - (pty - t)
                if (pt>imageHeight-100f) pt = imageHeight-110f
                Log.d(TAG, "moveMove: top")
            } else {
                pt = 0f
                pb = b-t
                Log.d(TAG, "moveMove: top 2")
            }

            if (motionY+b-pty<imageHeight) {
                pb = motionY + b - pty
                if (pb<100f) pb = 110f
                Log.d(TAG, "moveMove: bottom")
            } else {
                pb = imageHeight.toFloat()
                Log.d(TAG, "moveMove: bottom 2")
            }



            rectF.set(pl+5, pt + 5, pr - 5, pb - 5)
            invalidate()

        }

        // moving while holding corners
        if (c6) {
            if (motionX > 0 && motionX < (pr - 100)) pl = motionX
            if (motionY > 0 && motionY < (pb - 100)) pt = motionY
        }
        if (c7) {
            if (motionY > 0 && motionY < (pb - 100)) pt = motionY
            if (motionX > (pl + 100) && motionX < screenWidth) pr = motionX
        }
        if (c8) {
            if (motionX > (pl + 100) && motionX < screenWidth) pr = motionX
            if (motionY > (pt + 100) && motionY < imageHeight) pb = motionY
        }
        if (c9) {
            if (motionX > 0 && motionX < (pr - 100)) pl = motionX
            if (motionY > (pt + 100) && motionY < imageHeight) pb = motionY
        }

        // For moving the edge
        if (c1) if (motionX > 0 && motionX < (pr - 100)) pl = motionX
        if (c2) if (motionY > 0 && motionY < (pb - 100)) pt = motionY
        if (c3) if (motionX > (pl + 100) && motionX < screenWidth) pr = motionX
        if (c4) if (motionY > (pt + 100) && motionY < imageHeight) pb = motionY


        rectF.set(pl + 5, pt + 5, pr - 5, pb - 5)
        invalidate()

    }

    private fun moveDown() {

        if (motionX > (pl + rng_h) && motionX < (pr - rng_h) && motionY > (pt + rng_v) && motionY < (pb - rng_v)) {

            c5 = true
            l = pl
            t = pt
            r = pr
            b = pb

            if (motionY >= 0 && motionY <= imageHeight) pty = motionY
            if (motionX >= 0 && motionX <= screenWidth) plx = motionX

            invalidate()
            return
        }




        if (motionX in pl - rng_h..pl + rng_h && motionY in pt - rng_v..pt + rng_v) {
            c6 = true
            invalidate()
            return
        }
        if (motionY in pt - rng_v..pt + rng_v && motionX in pr - rng_h..pr + rng_h) {
            c7 = true
            invalidate()
            return
        }
        if (motionX in pr - rng_h..pr + rng_h && motionY in pb - rng_v..pb + rng_v) {
            c8 = true
            invalidate()
            return
        }
        if (motionY in pb - rng_v..pb + rng_v && motionX in pl - rng_h..pl + rng_h) {
            c9 = true
            invalidate()
            return
        }




        if (motionX > (pl - rng_h) && motionX < (pl + rng_h) && motionY > pt && motionY < pb) {
            c1 = true
            invalidate()
            return
        }
        if (motionY > (pt - rng_v) && motionY < (pt + rng_v) && motionX > pl && motionX < pr) {
            c2 = true
            invalidate()
            return
        }
        if (motionX > (pr - rng_h) && motionX < (pr + rng_h) && motionY > pt && motionY < pb) {
            c3 = true
            invalidate()
            return
        }
        if (motionY > (pb - rng_v) && motionY < (pb + rng_v) && motionX > pl && motionX < pr) {
            c4 = true
            invalidate()
            return
        }


        invalidate()

    }

    private fun moveUp() {
        c1 = false
        c2 = false
        c3 = false
        c4 = false
        c5 = false

        c6 = false
        c7 = false
        c8 = false
        c9 = false

        invalidate()
    }


}
