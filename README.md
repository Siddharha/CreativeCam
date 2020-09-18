# CreativeCam
# How To Use

## Request For the CamActivity
	 val intent_cam = Intent(this,CamViewActivity::class.java)
        intent_cam.putExtra(CamUtil.CAM_FACING,1)			//For What Camera to use
        intent_cam.putExtra(CamUtil.CAM_SWITCH_OPT,false)		//For Font and back cam switch options
	
        intent_cam.putExtra(CamUtil.CAPTURE_BTN_COLOR,"#00bcd4")	//to change color of Capture button
        intent_cam.putExtra(CamUtil.CAPTURE_CONTROL_COLOR,"#ffffff")
	
        startActivityForResult(intent_cam,CREATIVE_CAM_REQ)		//requesting for capture photo
## Get Image For use
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
	
# To use the library add below code to your project 

  	allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
    }

# Gradle
  	dependencies {
	        implementation 'com.github.Siddharha:CreativeCam:1.0.3'
	}
