package cz.utb.photostudio.camera

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Camera

class CameraService {
    companion object {

        private val TAG = "CAMERA_SERVICE"

        /**
         * Overi zda zariveni ma kameru
         */
        fun checkCameraHardware(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }

        /**
         *  Navrati instanci kamery
         */
        fun getCameraInstance(): Camera? {
            return try {
                Camera.open() // attempt to get a Camera instance
            } catch (e: Exception) {
                // Camera is not available (in use or does not exist)
                null // returns null if camera is unavailable
            }
        }

        val defaultPictureCallback = Camera.PictureCallback { data, _ ->

        }

    }
}