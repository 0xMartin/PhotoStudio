package cz.utb.photostudio.camera

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log

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
                var cam = Camera.open()
                cam.autoFocus(this.defaultFocusCallback);
                return cam
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Defaultni callback pro zpracovani porizeneho snimku
         */
        val defaultPictureCallback = Camera.PictureCallback { data, camera ->
            {
                Log.d(TAG, "Picture taken!}")
            }
        }

        /**
         * Defaultni callback pro zpracovani zaostrani
         */
        val defaultFocusCallback = Camera.AutoFocusCallback { success, camera ->

        };

    }
}