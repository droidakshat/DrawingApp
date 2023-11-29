package ak.drawingapp

import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Message
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var DrawingView: drawingView? = null
    private var mImageButtonCurrrentPaint: ImageButton? = null

    val openGalleryLauncher:ActivityResultLauncher<Intent>
    = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== RESULT_OK && result.data!=null){
            val imageBackground:ImageView=findViewById(R.id.iv_background)
            imageBackground.setImageURI(result.data?.data)
        }
    }
    val requestPermission:ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions->
            permissions.entries.forEach{
             val permissionName=it.key
             var isGranted =it.value
                if(isGranted){
                    Toast.makeText(
                        this@MainActivity,
                        "Permission Granted",
                        Toast.LENGTH_LONG).show()
                    val pickInent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickInent)


                }else{
                    if (permissionName==Manifest.permission.READ_MEDIA_IMAGES){
                        Toast.makeText(
                            this@MainActivity,
                            "Permission Not Granted",
                            Toast.LENGTH_LONG).show()



                    }
                }
            }

        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            DrawingView = findViewById(R.id.drawing_view)
            DrawingView?.setSizeBrush(20f)

            val linearLayoutPAintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
            mImageButtonCurrrentPaint = linearLayoutPAintColors[2] as ImageButton
            mImageButtonCurrrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)

            )

            val ib_brush: ImageButton = findViewById(R.id.ib_brush)
            ib_brush.setOnClickListener {
                showBrushSizeChooseDialog()
            }
            val gallery: ImageButton = findViewById(R.id.ib_gallery)
            gallery.setOnClickListener {

                 requestStoragePermission()
            }
            val ibundo:ImageButton=findViewById(R.id.ib_undo)
            ibundo.setOnClickListener {
                    DrawingView?.onClickUndo()
            }
            val ibSave:ImageButton=findViewById(R.id.ib_save)
            ibSave.setOnClickListener {
                if (isReadStoreageAllowed()){
                    lifecycleScope.launch(){
                        val fldrawingView:FrameLayout=findViewById(R.id.fl_drawing_view_container)
                        val mybitmap:Bitmap=getBitmapFromView(fldrawingView)
                        saveBitMapFile(mybitmap)


                    }
                }

            }


        }


        private fun showBrushSizeChooseDialog() {
            val brushDialog = Dialog(this)
            brushDialog.setContentView(R.layout.dialog_brush_size)
            brushDialog.setTitle("Brush Size")
            var smallBtn: ImageButton = brushDialog.findViewById(R.id.small_brush)
            smallBtn.setOnClickListener {
                DrawingView?.setSizeBrush(10f)
                brushDialog.dismiss()
            }
            var mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
            mediumBtn.setOnClickListener {
                DrawingView?.setSizeBrush(20f)
                brushDialog.dismiss()
            }
            var largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
            largeBtn.setOnClickListener {
                DrawingView?.setSizeBrush(30f)
                brushDialog.dismiss()
            }

            brushDialog.show()

        }

        fun paintClicked(view: View) {
            if (view !== mImageButtonCurrrentPaint) {
                val imageButton = view as ImageButton
                val colorTag = imageButton.tag.toString()
                DrawingView?.setColor(colorTag)

                imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
                )
                mImageButtonCurrrentPaint?.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)

                )
                mImageButtonCurrrentPaint = view
            }
        }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            showRationalDialog("Drawing App","Drawing App"+"needs to Acess your exernal storage")
        }else{
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }
    private fun isReadStoreageAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_MEDIA_IMAGES
        )
        return result==PackageManager.PERMISSION_GRANTED
    }

        private fun showRationalDialog(
            title: String,
            message: String
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("cancle") { dialog, _ ->
                    dialog.dismiss()

                }
            builder.create().show()
        }
    private fun getBitmapFromView(view:View):Bitmap{
        val returnBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(returnBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }
     private suspend fun saveBitMapFile(mbitmap:Bitmap?):String{

         var result=""
         withContext(Dispatchers.IO){
             if(mbitmap!=null){
                 try{
                     val byte=ByteArrayOutputStream()
                     mbitmap.compress(Bitmap.CompressFormat.PNG,90,byte)

                     val f= File(externalCacheDir?.absoluteFile.toString()
                     +File.separator+  "Drawing App"+System.currentTimeMillis()/1000+".png")

                      val fo=FileOutputStream(f)
                     fo.write(byte.toByteArray())
                     fo.close()

                     result=f.absolutePath

                     runOnUiThread{
                         if(result.isNotEmpty()){
                             Toast.makeText(this@MainActivity,
                                 "file saved succesfully :$result"
                                    ,Toast.LENGTH_LONG).show()
                             shareImage(result)
                         }else{
                             Toast.makeText(this@MainActivity,
                                 "Somthing went wrong!! :$result"
                                 ,Toast.LENGTH_LONG).show()

                         }
                     }
                 }catch (e:Exception){
                     result=""
                     e.printStackTrace()
                 }
             }
         }
         return result
     }
    private fun shareImage(result:String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path,uri->
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type="Image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }
    }




