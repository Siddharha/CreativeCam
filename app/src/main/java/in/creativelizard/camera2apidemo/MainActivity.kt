package `in`.creativelizard.camera2apidemo

import `in`.creativelizard.creativecam.CamUtil
import `in`.creativelizard.creativecam.CamViewActivity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

val CREATIVE_CAM_REQ = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        onActionPerform()
    }

    private fun initialize() {

    }

    private fun onActionPerform() {
        btnTakeImage.setOnClickListener {
            openImageCapture()
        }
    }

    private fun openImageCapture() {
        val intent_cam = Intent(this,CamViewActivity::class.java)
        intent_cam.putExtra(CamUtil.CAM_FACING,1)
        intent_cam.putExtra(CamUtil.CAM_SWITCH_OPT,false)
        intent_cam.putExtra(CamUtil.CAPTURE_BTN_COLOR,"#00bcd4")
        intent_cam.putExtra(CamUtil.CAPTURE_CONTROL_COLOR,"#ffffff")
        startActivityForResult(intent_cam,CREATIVE_CAM_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val path = data?.getStringExtra(CamUtil.IMG_FILE_PATH)

        //val myBitmap = BitmapFactory.decodeFile(path)
        //imgDisplay.setImageBitmap(myBitmap)

        val imageUri = Uri.fromFile(File(path!!))
       // Log.e("path",imageUri.path!!)
        Glide.with(this)
            .load(imageUri)
            .into(imgDisplay)

        //imgDisplay.setImageURI(imageUri)
    }
}


