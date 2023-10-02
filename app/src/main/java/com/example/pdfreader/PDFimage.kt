package com.example.pdfreader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.pow
import kotlin.math.sqrt


@SuppressLint("AppCompatCustomView")
class PDFimage  // constructor
    (context: Context?) : ImageView(context) {

    // drawing path
    var path: Path? = null

    // image to display
    var bitmap: Bitmap? = null

    var pen = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5F
    }
    var highlighter = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 30F
        setARGB(70,255,255,2)
    }
    val eraser = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = 30F
    }

    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    var prevX1 = 0f
    var prevY1 = 0f
    var prevX2 = 0f
    var prevY2 = 0f
    var dx = 0f
    var dy = 0f
    var currentMatrix = Matrix()

    init {
        // Disable hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        // Create a new matrix and compute the inverse of currentMatrix
        val invertedMatrix = Matrix()
        currentMatrix.invert(invertedMatrix)

        // Convert the touch coordinates
        val touchCoords = floatArrayOf(event.x, event.y)
        invertedMatrix.mapPoints(touchCoords)

        when(event.pointerCount) {
            1 -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (curTool != "hand") {
                            path = Path()
                            // Use the converted coordinates instead of the original ones
                            path!!.moveTo(touchCoords[0], touchCoords[1])
                        } else {
                            prevX1 = event.x
                            prevY1 = event.y
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (curTool != "hand") {
                            path!!.lineTo(touchCoords[0], touchCoords[1])
                        } else {
                            dx += event.x - prevX1
                            dy += event.y - prevY1
                            currentMatrix.preTranslate(event.x - prevX1, event.y - prevY1)
                            prevX1 = event.x
                            prevY1 = event.y
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (curTool != "hand") {
                            path?.let { Op(path!!, curTool, current, true) }?.let { Ops.add(it) }
                            path = null
                            undo.push(Ops.last());
                        }
                    }
                }
            }
            2 -> {
                val transformationMatrix = Matrix()
                if (prevX1 < 0 || prevY1 < 0) {
                    prevX1 = event.getX(0)
                    prevY1 = event.getY(0)
                } else {
                    prevX1 = x1
                    prevY1 = y1
                }
                if (prevX2 < 0 || prevY2 < 0) {
                    prevX2 = event.getX(1)
                    prevY2 = event.getX(1)
                } else {
                    prevX2 = x2
                    prevY2 = y2
                }
                x1 = event.getX(0)
                y1 = event.getY(0)
                x2 = event.getX(1)
                y2 = event.getY(1)
                if (event.action == MotionEvent.ACTION_MOVE) {
                    val scale = sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2)) / sqrt((prevX1 - prevX2).pow(2) + (prevY1 - prevY2).pow(2))
                    transformationMatrix.postScale(scale, scale, (x1 + x2) / 2, (y1 + y2) / 2)
                    currentMatrix.postConcat(transformationMatrix)
                }
            }
        }
        return true
    }

    // set image as background
    fun setImage(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    fun setScale(orientation: Int) {
        if (orientation == 1) currentMatrix.preTranslate(-250f, -100f)
        else currentMatrix.preTranslate(150f, 100f)

    }


    private fun draw(canvas: Canvas, tool: String, p: Path?) {
        if (tool == "pen") {
            canvas.drawPath(p!!, pen)
        } else if (tool == "highlighter"){
            canvas.drawPath(p!!, highlighter)
        } else {
            canvas.drawPath(p!!, eraser)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.concat(currentMatrix)
        if (bitmap != null) {
            setImageBitmap(bitmap)
        }
        for (op in Ops) {
            if (op.page == current && op.isVisible) {
                draw(canvas, op.tool, op.path)
            }
        }
        if (path != null) {
            draw(canvas, curTool, path!!)
        }
        super.onDraw(canvas)
    }
}