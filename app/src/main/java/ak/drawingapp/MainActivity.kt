package ak.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var  DrawingView:drawingView?=null
    private var mImageButtonCurrrentPaint:ImageButton?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawingView=findViewById(R.id.drawing_view)
        DrawingView?.setSizeBrush(20f)

        val linearLayoutPAintColors=findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrrentPaint=linearLayoutPAintColors[2] as ImageButton
        mImageButtonCurrrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)

        )

        val ib_brush:ImageButton=findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSizeChooseDialog()
        }

    }
    private fun showBrushSizeChooseDialog(){
        val brushDialog=Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        var smallBtn:ImageButton = brushDialog.findViewById(R.id.small_brush)
        smallBtn.setOnClickListener{
            DrawingView?.setSizeBrush(10f)
            brushDialog.dismiss()
        }
        var mediumBtn:ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            DrawingView?.setSizeBrush(20f)
            brushDialog.dismiss()
        }
        var largeBtn:ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            DrawingView?.setSizeBrush(30f)
            brushDialog.dismiss()
        }

brushDialog.show()

    }
    fun paintClicked(view: View){
     if (view!==mImageButtonCurrrentPaint){
         val imageButton=view as ImageButton
         val colorTag =imageButton.tag.toString()
         DrawingView?.setColor(colorTag)

         imageButton.setImageDrawable(
             ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
         )
         mImageButtonCurrrentPaint?.setImageDrawable(
             ContextCompat.getDrawable(this,R.drawable.pallet_normal)

         )
         mImageButtonCurrrentPaint=view
     }
    }
}
