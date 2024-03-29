package `in`.creativelizard.creativecam

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.cam_activity_layout.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CamViewActivity : AppCompatActivity() {



    private lateinit var camera:Camera
    private val cameraExecutor:ExecutorService by lazy {Executors.newSingleThreadExecutor()}
    private val mPreview:PreviewView by lazy { findViewById(R.id.pvPreview) }
    private  val imagePreview: Preview by lazy{Preview.Builder().apply {
        try{
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setTargetRotation(mPreview.display.rotation)
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            print("ex happend!")
        }

    }.build()}

    private val imageCapture: ImageCapture by lazy {ImageCapture.Builder().apply {
        setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        setFlashMode(ImageCapture.FLASH_MODE_AUTO)
    }.build()}

    private var facing = 1
    private val cameraProviderFuture:ListenableFuture<ProcessCameraProvider> by lazy {ProcessCameraProvider.getInstance(this)}
    private val cameraProvider:ProcessCameraProvider by lazy{cameraProviderFuture.get()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cam_activity_layout)
        initialize()
        onActionPerform()
    }

    private fun onActionPerform() {
        fabCapture.setOnClickListener {
            takePicture()


        }
        fabCamSwitch.setOnClickListener {
                switchCamera()


        }
    }

    private fun switchCamera() {
        cameraProvider.unbindAll()
        if(facing ==1) {
            facing = 0
            startCamera(facing)
        }else{
            facing = 1
            startCamera(facing)
        }
    }

    private fun initialize(){

        if (intent.hasExtra(CamUtil.TARGET_BOX)) {
            //TARGET_COLOR
        imgTargetBox.visibility = intent.getIntExtra(CamUtil.TARGET_BOX,View.GONE)
            if(intent.hasExtra(CamUtil.TARGET_BOX_TYPE)){
               if( intent.getIntExtra(CamUtil.TARGET_BOX_TYPE,0)==1){
                    imgTargetBox.setImageDrawable(resources.getDrawable(R.drawable.target_box_wide))
                }

            }

        }

        if(intent.hasExtra(CamUtil.TARGET_COLOR)){
            val drawable = imgTargetBox.drawable as GradientDrawable
            drawable.setStroke(intent.getIntExtra(CamUtil.TARGET_WIDTH,1),
                intent.getIntExtra(CamUtil.TARGET_COLOR, Color.TRANSPARENT),
            10f,
                1f) // set stroke width and stroke color

        /*.setColorFilter(
                intent.getIntExtra(CamUtil.TARGET_COLOR, Color.TRANSPARENT)
            )*/
        }
                if (intent.hasExtra(CamUtil.CAPTURE_BTN_COLOR)) {
            fabCapture.backgroundTintList = ColorStateList.valueOf(  intent.getIntExtra(CamUtil.CAPTURE_BTN_COLOR,
                Color.BLACK))
        }
        if (intent.hasExtra(CamUtil.CAPTURE_BTN_ICON_COLOR)) {
            fabCapture.setColorFilter(  intent.getIntExtra(CamUtil.CAPTURE_BTN_ICON_COLOR,Color.WHITE))
        }

        //SWITCH_CAM_BTN_ICON_COLOR
        if (intent.hasExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR)) {
            fabCamSwitch.setColorFilter(intent.getIntExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR,Color.WHITE))
        }

        if (intent.hasExtra(CamUtil.SWITCH_CAM_BTN_COLOR)) {
            fabCamSwitch.backgroundTintList = ColorStateList.valueOf(  intent.getIntExtra(CamUtil.SWITCH_CAM_BTN_COLOR,Color.BLACK))
        }

        if (intent.hasExtra(CamUtil.CAPTURE_CONTROL_COLOR)) {
            fmControl.setBackgroundColor(intent.getIntExtra(CamUtil.CAPTURE_CONTROL_COLOR,Color.WHITE))
        }

        if (intent.getBooleanExtra(CamUtil.CAM_SWITCH_OPT,false)) {
                    fabCamSwitch.visibility = View.VISIBLE
                }else{
                    fabCamSwitch.visibility = View.GONE
                }

         facing = intent.getIntExtra(CamUtil.CAM_FACING,0)

        if(facing ==0){
            facing = CameraSelector.LENS_FACING_FRONT
        }else{
            facing = CameraSelector.LENS_FACING_BACK
        }

        cameraProvider.unbindAll()
        startCamera(facing)
    }

    private fun startCamera(facing: Int) {
// Create preview use case

        try {
            val cameraSelector = CameraSelector.Builder().requireLensFacing(facing).build()
            cameraProviderFuture.addListener({
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, imagePreview)


                mPreview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                imagePreview.setSurfaceProvider(mPreview.surfaceProvider)
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            }, ContextCompat.getMainExecutor(this))

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun takePicture() {
//        val file = File(
//            Environment.getExternalStorageDirectory().toString() + "/" + UUID.randomUUID()
//                .toString() + ".jpg"
//        )
        val file = createFile(
            getOutputDirectory(),
            FILENAME,
            PHOTO_EXTENSION
        )
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor, object : ImageCapture.
        OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                val msg = "Photo capture succeeded: ${file.absolutePath}"
                mPreview.post {
                    cameraProvider.unbindAll()
                    //Toast.makeText(this@CamViewActivity, msg, Toast.LENGTH_LONG).show()

                                    val resultIntent =  Intent()
                resultIntent.putExtra(CamUtil.IMG_FILE_PATH, file.absolutePath )

                setResult(Activity.RESULT_OK,resultIntent)
                    //releaseInstance()

                finish()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                val msg = "Photo capture failed: ${exception.message}"
                mPreview.post {
                    Toast.makeText(this@CamViewActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }

        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, UUID.randomUUID().toString()).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

        fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)
    }

}
