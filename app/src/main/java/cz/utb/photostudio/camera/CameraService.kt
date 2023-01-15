package cz.utb.photostudio.camera

import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.NonNull


class CameraService {

    private val TAG: String = "CAM_SERVICE"

    private val ORIENTATIONS = SparseIntArray()

    private var context: Context? = null
    private var textureView : TextureView? = null

    private var DSI_width: Int = 0
    private var DSI_height: Int = 0

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null

    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    private var effect: Int = CaptureRequest.CONTROL_EFFECT_MODE_OFF

    /**********************************************************************************************/
    // PUBLIC SECTION START
    /**********************************************************************************************/

    fun initService(context: Context, textureView: TextureView) {
        val displayMetrics = context.resources.displayMetrics
        DSI_height = displayMetrics.heightPixels
        DSI_width = displayMetrics.widthPixels

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        this.context = context
        textureView.surfaceTextureListener = textureListener
        this.textureView = textureView

        Log.i(TAG, "Service inited.");
    }

    fun selectEffect(effect: Int) {
        if(effect == CaptureRequest.CONTROL_EFFECT_MODE_AQUA ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_MONO ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_SEPIA ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD ||
            effect == CaptureRequest.CONTROL_EFFECT_MODE_OFF) {
            this.effect = effect
            applyEffect(captureRequestBuilder)
            updatePreview()
        }
    }

    fun startService() {
        if(mBackgroundThread == null || mBackgroundHandler == null) {
            mBackgroundThread = HandlerThread("Camera Background")
            mBackgroundThread!!.start()
            mBackgroundHandler = Handler(mBackgroundThread!!.looper)
            if (textureView!!.isAvailable) {
                this.openCamera()
            } else {
                textureView!!.surfaceTextureListener = textureListener
            }
            Log.i(TAG, "Service is running now.");
        }
    }

    fun stopService() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.i(TAG, "Service is stopped.");
    }

    fun takePicture() {
        if (cameraDevice == null) {
            Log.e(TAG, "CameraDevice is null.");
            return
        }
        if (imageDimension == null) {
            Log.e(TAG, "Image dimension is null");
            return
        }

        val imageReader: ImageReader = ImageReader.newInstance(imageDimension!!.width, imageDimension!!.height, ImageFormat.JPEG, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            // ##################################################
            Log.i(TAG, "Picture taken");
        }, null)

        val surfaces = listOf(imageReader.surface)
        this.cameraDevice!!.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                val captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                    this?.addTarget(imageReader.surface)
                }
                if (captureRequest != null) {
                    session.capture(captureRequest.build(), null, null)
                }
            }

            override fun onConfigureFailed(p0: CameraCaptureSession) {}
        }, null)
    }

    /**********************************************************************************************/
    // PUBLIC SECTION END
    /**********************************************************************************************/

    private fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)

            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            applyEffect(captureRequestBuilder)

            cameraDevice!!.createCaptureSession(listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(context, "Changed", Toast.LENGTH_SHORT).show()
                    }
                },
                null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.i(TAG, "Camera preview created.");
    }

    private fun updatePreview() {
        if (cameraDevice == null) Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) {
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth: Int = DSI_width
            val newHeight: Int = DSI_width * ResolutionWidth / ResolutionHeight
            textureView!!.layoutParams = FrameLayout.LayoutParams(newWidth, newHeight)
        } else {
            val newWidth: Int = DSI_width
            val newHeight: Int = DSI_width * ResolutionHeight / ResolutionWidth
            textureView!!.layoutParams = FrameLayout.LayoutParams(newWidth, newHeight)
        }
    }

    private fun openCamera(): Boolean {
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        if(manager == null) {
            Log.e(TAG, "CameraManager is NULL");
            return false
        }
        try {
            this.cameraId = manager!!.cameraIdList[0]
            if(cameraId == null) {
                Log.e(TAG, "Faild to open camera. CameraID is NULL");
                return false
            }
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    private fun applyEffect(requestBuilder: CaptureRequest.Builder?) {
        if(requestBuilder == null) return
        requestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, this.effect);
    }

    private var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            setAspectRatioTextureView(imageDimension!!.width, imageDimension!!.height)
        }
    }

    private var stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
            Log.i(TAG, "The camera was opened");
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            Log.i(TAG, "The camera was closed");
        }

        override fun onError(cameraDevice: CameraDevice, i: Int) {
            cameraDevice.close()
            Log.e(TAG, "An error has occured and the camera was closed");
        }
    }

}