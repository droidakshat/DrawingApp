package ak.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.viewmodel.CreationExtras


class drawingView(context: Context,attri:AttributeSet): View(context,attri) {

    private var mDrawPath:CustomPath?=null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mBrushSize:Float= 0.toFloat()
    private var mCanvasPaint:Paint?=null
    private var canvas:Canvas?=null
    private var color= Color.BLACK
    private var mPath=ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint= Paint()
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
      //  mBrushSize= 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
         canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        //to draw somthing on the canvas
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for (path in mPath){
            mDrawPaint!!.strokeWidth=path.brushThick
            mDrawPaint!!.color= path.colour
            canvas?.drawPath(path,mDrawPaint!!)

        }
        if(!mDrawPath!!.isEmpty){//!mpath!!.isEmpty
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThick
            mDrawPaint!!.color=mDrawPath!!.colour
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                mDrawPath!!.colour = color
                mDrawPath!!.brushThick = mBrushSize

                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }


            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                mPath.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            }
            else -> return false
        }
            invalidate()

        return true
    }
    fun setSizeBrush(newSize:Float){
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }
    fun setColor(newcolor:String){
        color=Color.parseColor(newcolor)
        mDrawPaint!!.color=color
    }



    internal inner class CustomPath(var colour:Int,var brushThick:Float):Path() {

    }
}