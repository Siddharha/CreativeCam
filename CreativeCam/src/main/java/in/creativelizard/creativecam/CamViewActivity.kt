package `in`.creativelizard.creativecam

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.util.DisplayMetrics
import android.util.Size
import android.util.SparseIntArray
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.cam_activity_layout.*
import org.jetbrains.anko.activityUiThread
import org.jetbrains.anko.doAsync
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class CamViewActivity : AppCompatActivity() {

    private val ORIENTATIONS: SparseIntArray = SparseIntArray()

    val REQUEST_CAMERA_PERMISSION = 1
    var camIntNo = 0
    var file: File?=null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var cameraDevice: CameraDevice? = null
    var stateCallback : CameraDevice.StateCallback?=null
    var textureListener: TextureView.SurfaceTextureListener?=null
    var cameraId:String ?=null
    private var imageDimension: Size? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cam_activity_layout)
        initialize()
        setupTexture()

        onActionPerform()
    }

    private fun onActionPerform() {
        fabCapture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePicture()
            }
        }

        fabCamSwitch.setOnClickListener {
            val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            when (camIntNo){
                0 -> {
                    camIntNo = 1
                    cameraDevice?.close()
                    if(txMain.isAvailable)
                        openCamera()
                    else
                        txMain.surfaceTextureListener = textureListener
                }
                1 -> {camIntNo = 0
                    cameraDevice?.close()
                    if(txMain.isAvailable)
                        openCamera()
                    else
                        txMain.surfaceTextureListener = textureListener

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takePicture() {
        if (cameraDevice == null) return
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val characteristics =
            manager.getCameraCharacteristics(cameraDevice!!.id)
        var jpegSizes: Array<Size>? = null
        if (characteristics != null) jpegSizes =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)

        //Capture image with custom size

        //Capture image with custom size
        var width = 640
        var height = 480
        if (jpegSizes != null && jpegSizes.isNotEmpty()) {
            width = jpegSizes[0].width
            height = jpegSizes[0].height
        }

        val reader: ImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurface: MutableList<Surface> = ArrayList(2)
        outputSurface.add(reader.surface)
        outputSurface.add(Surface(txMain.surfaceTexture))

        val captureBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(reader.surface)
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        //Check orientation base on device

        //Check orientation base on device
        val rotation = windowManager.defaultDisplay.rotation


        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
        if(intent.getIntExtra(CamUtil.CAM_FACING,0) == 1){
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270)
        }
        file = File(
            Environment.getExternalStorageDirectory().toString() + "/" + UUID.randomUUID()
                .toString() + ".jpg"
        )

        val readerListener = ImageReader.OnImageAvailableListener { imageReader ->
            var image: Image? = null
            try {
                image = reader.acquireLatestImage()
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                save(bytes)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                {
                    if (image != null) image.close()
                }
            }
        }

        reader.setOnImageAvailableListener(readerListener,mBackgroundHandler)
        val captureListener = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                Toast.makeText(this@CamViewActivity, "Saved "+file, Toast.LENGTH_SHORT).show();
                createCameraPreview()
                val resultIntent =  Intent()
                resultIntent.putExtra(CamUtil.IMG_FILE_PATH, file?.absolutePath )

                setResult(Activity.RESULT_OK,resultIntent)
                finish()

            }
        }

        cameraDevice?.createCaptureSession(outputSurface,object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {

            }

            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    session.capture(
                        captureBuilder.build(),
                        captureListener,
                        mBackgroundHandler
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

        },mBackgroundHandler)


    }

    private fun save(bytes: ByteArray) {
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file!!)
            outputStream.write(bytes)
        } finally {
            outputStream?.close()
        }
    }



    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if(txMain.isAvailable)

            openCamera()
        else
            txMain.surfaceTextureListener = textureListener
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    private fun stopBackgroundThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBackgroundThread?.quitSafely()
        }
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startBackgroundThread() {

        camIntNo = intent.getIntExtra(CamUtil.CAM_FACING,0)
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread?.start()
        mBackgroundHandler =  Handler(mBackgroundThread?.looper!!)
    }

    private fun initialize() {

        if (intent.hasExtra(CamUtil.CAPTURE_BTN_COLOR)) {
            fabCapture.backgroundTintList = ColorStateList.valueOf(  intent.getIntExtra(CamUtil.CAPTURE_BTN_COLOR,Color.BLACK))
        }
        if (intent.hasExtra(CamUtil.CAPTURE_BTN_ICON_COLOR)) {
            fabCapture.setColorFilter(  intent.getIntExtra(CamUtil.CAPTURE_BTN_ICON_COLOR,Color.BLACK))
        }

        //SWITCH_CAM_BTN_ICON_COLOR
        if (intent.hasExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR)) {
            fabCamSwitch.setColorFilter(intent.getIntExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR,Color.BLACK))
        }

        if (intent.hasExtra(CamUtil.SWITCH_CAM_BTN_COLOR)) {
            fabCamSwitch.backgroundTintList = ColorStateList.valueOf(  intent.getIntExtra(CamUtil.SWITCH_CAM_BTN_COLOR,Color.BLACK))
        }

        if (intent.hasExtra(CamUtil.CAPTURE_CONTROL_COLOR)) {
            fmControl.setBackgroundColor(intent.getIntExtra(CamUtil.CAPTURE_CONTROL_COLOR,Color.WHITE))
        }
        ORIENTATIONS.append(Surface.ROTATION_0,90)
        ORIENTATIONS.append(Surface.ROTATION_90,0)
        ORIENTATIONS.append(Surface.ROTATION_180,270)
        ORIENTATIONS.append(Surface.ROTATION_270,180)

        textureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return false
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                openCamera()
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice?.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cameraDevice?.close()
                    cameraDevice=null
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createCameraPreview() {
        try {
            val texture: SurfaceTexture = txMain.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            setAspectRatioTextureView(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            // captureRequestBuilder?.set(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_SEPIA)
            captureRequestBuilder?.addTarget(surface)
            cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@CamViewActivity, "Changed", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch ( e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updatePreview() {
        if (cameraDevice == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

        try {

            cameraCaptureSessions!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, mBackgroundHandler)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun openCamera() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                if (intent.getBooleanExtra(CamUtil.CAM_SWITCH_OPT,false)) {
                    fabCamSwitch.visibility = View.VISIBLE
                }else{
                    fabCamSwitch.visibility = View.GONE
                }

                if (manager.cameraIdList.size<2){

                        fabCamSwitch.visibility = View.GONE

                }
                cameraId = manager.cameraIdList[camIntNo]
                val characteristics = manager.getCameraCharacteristics(cameraId!!)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
                //Check realtime permission if run higher API 23
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CAMERA_PERMISSION)
                    return
                }
                manager.openCamera(cameraId!!, stateCallback!!, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun setupTexture() {
        txMain.surfaceTextureListener = textureListener
    }

    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) { //for resizing texture with camera image
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val  DSI_height = displayMetrics.heightPixels
        val  DSI_width = displayMetrics.widthPixels
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth: Int = DSI_width
            val newHeight: Int = DSI_width * ResolutionWidth / ResolutionHeight
            updateTextureViewSize(newWidth, newHeight)
        } else {
            val newWidth: Int = DSI_width
            val newHeight: Int = DSI_width * ResolutionHeight / ResolutionWidth
            updateTextureViewSize(newWidth, newHeight)
        }
    }

    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
        // Log.d(FragmentActivity.TAG, "TextureView Width : $viewWidth TextureView Height : $viewHeight")

        doAsync {
            val param =  FrameLayout.LayoutParams(viewWidth, viewHeight)
            param.gravity = Gravity.CENTER

            activityUiThread{
                txMain.layoutParams = param


            }

        }

    }


}
