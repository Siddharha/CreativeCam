package `in`.creativelizard.camera2apidemo

import `in`.creativelizard.creativecam.CamUtil
import `in`.creativelizard.creativecam.CamViewActivity
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.android.synthetic.main.activity_second.imgDisplay
import java.io.File

class SecondActivity : AppCompatActivity() {
    val CREATIVE_CAM_REQ = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        onActionPerform()
    }

    private fun onActionPerform() {
        btnft.setOnClickListener {
            val intent_cam = Intent(this, CamViewActivity::class.java)
            intent_cam.putExtra(CamUtil.CAM_FACING,0)
            intent_cam.putExtra(CamUtil.CAM_SWITCH_OPT,true)
            intent_cam.putExtra(CamUtil.CAPTURE_BTN_COLOR, Color.BLUE)
            intent_cam.putExtra(CamUtil.CAPTURE_BTN_ICON_COLOR, Color.MAGENTA)

            intent_cam.putExtra(CamUtil.SWITCH_CAM_BTN_COLOR, Color.YELLOW)
            intent_cam.putExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR, Color.BLACK)
            intent_cam.putExtra(CamUtil.TARGET_BOX, View.VISIBLE)
            intent_cam.putExtra(CamUtil.TARGET_COLOR, Color.parseColor("#00bcd4"))
            intent_cam.putExtra(CamUtil.TARGET_WIDTH, 5)
            intent_cam.putExtra(CamUtil.CAPTURE_CONTROL_COLOR, Color.WHITE)
            startActivityForResult(intent_cam,CREATIVE_CAM_REQ)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATIVE_CAM_REQ && resultCode == Activity.RESULT_OK) {
            val path = data?.getStringExtra(CamUtil.IMG_FILE_PATH)

            //val myBitmap = BitmapFactory.decodeFile(path)
            //imgDisplay.setImageBitmap(myBitmap)

            val imageUri = Uri.fromFile(File(path!!))
            // Log.e("path",imageUri.path!!)
            Glide.with(this)
                .load(imageUri)
                .into(imgDisplay)
            imgDisplay.setBackgroundColor(Color.GREEN)
            //imgDisplay.setImageURI(imageUri)
        }
    }
}