# CreativeCam
his Library creaded for those who wanna use Custom simple Camera in there Android app. This project uses Camera API 2. Try out this library by adding simple gradle link to your project.

[![](https://jitpack.io/v/Siddharha/CreativeCam.svg)](https://jitpack.io/#Siddharha/CreativeCam)

# Look
![](https://github.com/Siddharha/CreativeCam/blob/master/app/src/main/res/drawable/img.png)

# How To Use

#### Request For the CamActivity
	 val intent_cam = Intent(this,CamViewActivity::class.java)
             intent_cam.putExtra(CamUtil.CAM_FACING,1)
             intent_cam.putExtra(CamUtil.CAM_SWITCH_OPT,true)
             
             intent_cam.putExtra(CamUtil.CAPTURE_BTN_COLOR,Color.BLUE)
             intent_cam.putExtra(CamUtil.CAPTURE_BTN_ICON_COLOR,Color.MAGENTA)
     
             intent_cam.putExtra(CamUtil.SWITCH_CAM_BTN_COLOR,Color.YELLOW)
             intent_cam.putExtra(CamUtil.SWITCH_CAM_BTN_ICON_COLOR,Color.BLACK)
     
             intent_cam.putExtra(CamUtil.CAPTURE_CONTROL_COLOR,Color.WHITE)
             startActivityForResult(intent_cam,CREATIVE_CAM_REQ)
#### Get Image For use
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
	
 # Gradle

  	allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
    }


  	dependencies {
	        implementation 'com.github.Siddharha:CreativeCam:Tag'
	}
 # License	
Copyright 2020 Siddhartha Maji

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
