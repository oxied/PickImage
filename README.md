# Fork of https://github.com/jrvansuita/PickImage
- Added support for multiple camera files and storing them on sdcard/Pictures/[AppName]
- Moved to AndroidX instead of android support library
- Added support for Android 8 (Oreo)

<!-- Library Logo -->
<img src="https://github.com/jrvansuita/PickImage/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png?raw=true" align="left" hspace="1" vspace="1">

# PickImage


This is an [**Android**](https://developer.android.com) project. It shows a [DialogFragment](https://developer.android.com/reference/android/app/DialogFragment.html) with Camera or Gallery options. The user can choose from which provider wants to pick an image.

</br> 
</br> 
</br> 

<!-- JitPack integration -->
[![JitPack](https://jitpack.io/v/oxied/PickImage.svg)](https://jitpack.io/#oxied/PickImage)

# Dialog screenshots

#### Default icons.
<img src="images/dialogs/light_vertical_left_simple.png" height='auto' width='280'/><img src="images/dialogs/light_horizontal_top_simple.png" height='auto' width='280'/><img src="images/dialogs/light_horizontal_left_simple.png" height='auto' width='280'/>

#### Colored icons.
<img src="images/dialogs/light_vertical_left_colored.png" height='auto' width='280'/><img src="images/dialogs/light_horizontal_top_colored.png" height='auto' width='280'/><img src="images/dialogs/light_horizontal_left_colored.png" height='auto' width='280'/>

#### Custom dialog.
<img src="images/dialogs/dark_vertical_left.png" height='auto' width='280'/><img src="images/dialogs/dark_horizontal_top.png" height='auto' width='280'/><img src="images/dialogs/dark_horizontal_left.png" height='auto' width='280'/>

#### System dialog.
<img src="images/dialogs/all_system_dialog.png" height='auto' width='280'/><img src="images/dialogs/camera_system_dialog.png" height='auto' width='280'/><img src="images/dialogs/images_system_dialog.png" height='auto' width='280'/> 
 
# Setup

#### Step #1. Add the JitPack repository to your build file:
```gradle
allprojects {
    repositories {
	...
	maven { url "https://jitpack.io" }
    }
}
```
     
#### Step #2. Add the dependency ([See latest release](https://jitpack.io/#oxied/PickImage)).
```groovy
dependencies {
    implementation 'com.github.oxied:PickImage:2.2.1'
}
```
# Implementation

#### Step #1. Overriding the library file provider authority to avoid installation conflicts.
The use of this library can cause [INSTALL_FAILED_CONFLICTING_PROVIDER](https://developer.android.com/guide/topics/manifest/provider-element.html#auth) if you skip this step. Update your AndroidManifest.xml with this exact provider declaration below.
```xml
<manifest ...>
    <application ...>
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.com.oxied.pickimage.provider"
            android:exported="false"
            android:grantUriPermissions="true"
            tools:replace="android:authorities">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/picker_provider_paths" />
        </provider>
    </application>	
</manifest> 
```

#### Step #2 - Showing the dialog.
```java
PickImageDialog.build(new PickSetup()).show(this);
``` 
#### Step #3 - Applying the listeners.

##### Method #3.1 - Make your AppCompatActivity implements IPickResult.
```java
@Override
public void onPickResult(PickResult r) {
    if (r.getError() == null) {
        //If you want the Uri.
        //Mandatory to refresh image from Uri.
        //getImageView().setImageURI(null);

        //Setting the real returned image.
        //getImageView().setImageURI(r.getUri());

        //If you want the Bitmap.
        getImageView().setImageBitmap(r.getBitmap());

        //Image path
        //r.getPath();
    } else {
        //Handle possible errors
        //TODO: do what you have to do with r.getError();
        Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
    }
}
```

##### Method #3.2 - Set the listener using the public method (Good for Fragments).

```java
PickImageDialog.build(new PickSetup())
               .setOnPickResult(new IPickResult() {
                  @Override
                  public void onPickResult(PickResult r) {
                     //TODO: do what you have to...
                  }
            }).show(getSupportFragmentManager());
```

#### Step #4 - Customize you Dialog using PickSetup.

```java
PickSetup setup = new PickSetup()
            .setTitle(yourText)
            .setTitleColor(yourColor)
            .setBackgroundColor(yourColor)
            .setProgressText(yourText)
            .setProgressTextColor(yourColor)
            .setCancelText(yourText)
            .setCancelTextColor(yourColor)
            .setButtonTextColor(yourColor)
            .setDimAmount(yourFloat)
            .setFlip(true)
            .setMaxSize(500)
            .setPickTypes(EPickType.GALLERY, EPickType.CAMERA)
            .setCameraButtonText(yourText)
            .setGalleryButtonText(yourText)
            .setIconGravity(Gravity.LEFT)
            .setButtonOrientation(LinearLayoutCompat.VERTICAL)
            .setSystemDialog(false)
            .setCameraToPictures(false)
            .setMinLoadingTime(2000); //in ms. 1000 is default to avoid dialog flipping
            .setGalleryIcon(yourIcon)
            .setCameraIcon(yourIcon);
/*... and more to come. */
```

# Additionals

#### Own click implementations.
If you want to write your own button click event, just use [IPickClick](library/src/main/java/com/vansuita/pickimage/listeners/IPickClick.java) listener like in the example below. You may want to take a look at the sample app.
 
 ```java
 PickImageDialog.build(setup)
         .setOnClick(new IPickClick() {
             @Override
             public void onGalleryClick() {
                 Toast.makeText(SampleActivity.this, "Gallery Click!", Toast.LENGTH_LONG).show();
             }

             @Override
             public void onCameraClick() {
                 Toast.makeText(SampleActivity.this, "Camera Click!", Toast.LENGTH_LONG).show();
             }
          }).show(this);
``` 

#### For dismissing the dialog.

```java
PickImageDialog dialog = PickImageDialog.build(...);
dialog.dismiss();
```

#### Force a specific width and height.
```java
new PickSetup().setWidth(600).setHeight(800);
```

     
# Sample app code.
 You can take a look at the sample app [located on this project](/app/).
