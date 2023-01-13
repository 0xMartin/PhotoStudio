package cz.utb.photostudio.camera

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log
import java.io.File

class CameraService {
    companion object {

        private final val TAG: String = "CAMERA_SERVICE"

        /**
         * Overi zda zariveni ma kameru
         */
        public fun checkCameraHardware(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }

        /**
         *  Navrati instanci kamery
         */
        public fun getCameraInstance(): Camera? {
            return try {
                Camera.open() // attempt to get a Camera instance
            } catch (e: Exception) {
                // Camera is not available (in use or does not exist)
                null // returns null if camera is unavailable
            }
        }

        public val defaultPictureCallback = Camera.PictureCallback { data, _ ->
            val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: run {
                Log.d(TAG, ("Error creating media file, check storage permissions"))
                return@PictureCallback
            }

        }

    }
}