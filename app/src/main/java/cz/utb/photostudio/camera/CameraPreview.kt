package cz.utb.photostudio.camera

import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import java.io.IOException

class CameraPreview(
    context: Context,
    private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback {

    private val TAG = "CAMERA_PREVIEW"

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        previewCamera(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (mHolder.surface == null) {
            return
        }
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error - stop camera preview: ${e.message}")
        }
        setCameraDisplayOrientation(mCamera);
        previewCamera(holder);
    }

    private fun previewCamera(holder: SurfaceHolder) {
        mCamera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d(TAG, "Error - setting camera preview: ${e.message}")
            }
        }
    }

    private fun setCameraDisplayOrientation(camera: Camera) {
        val parameters = camera.parameters
        val camInfo = CameraInfo()
        Camera.getCameraInfo(getBackFacingCameraId(), camInfo)
        val display: Display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).getDefaultDisplay()
        val rotation: Int = display.getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (camInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }


    private fun getBackFacingCameraId(): Int {
        var cameraId = -1
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
        }
        return cameraId
    }

}