package cz.utb.photostudio.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.OrientationEventListener
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout
import android.widget.Toast
import cz.utb.photostudio.config.GlobalConfig
import java.util.*


@Suppress("NAME_SHADOWING")
class CameraService {

    private val TAG: String = "CAM_SERVICE"

    // kontext app a textureview pro zobrazovani obrazu z kamery
    private var context: Context? = null
    private var textureView : TextureView? = null

    // velikost snimaneho obrazu
    private var DSI_width: Int = 0
    private var DSI_height: Int = 0

    // kamera
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null

    /********************************************************************************/
    // capture session
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var imageDimension: Size? = null

    // capture request pro textureview
    private var captureRequest_Preview: CaptureRequest.Builder? = null
    // image reader - pouzivany pro porizovani sniku z kamery
    private var imageReader: ImageReader? = null
    /********************************************************************************/

    // vlakno pro obnovovani obrazu z kamery
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    // aktualni zvoleny efekt
    private var effect: Int = CaptureRequest.CONTROL_EFFECT_MODE_OFF

    // callback pro porizeni sniku a zpracovani obrazu pomici ai
    private var takePictureCallback: ((img: Image)->Unit)? = null

    var copyBitmap: Bitmap? = null
    private var previewChangedCallback: ((bitmap: Bitmap)->Unit)? = null

    /**********************************************************************************************/
    // PUBLIC SECTION START
    /**********************************************************************************************/

    fun initService(context: Context, textureView: TextureView) {
        val displayMetrics = context.resources.displayMetrics
        DSI_height = displayMetrics.heightPixels
        DSI_width = displayMetrics.widthPixels

        this.context = context
        textureView.surfaceTextureListener = textureListener
        this.textureView = textureView

        Log.i(TAG, "Service inited.");
    }

    fun setTakePictureCallback(callback: (img: Image)->Unit) {
        this.takePictureCallback = callback
    }

    fun setPreviewChangedCallBack(callback: (bitmap: Bitmap)->Unit) {
        this.previewChangedCallback = callback
    }

    fun requestNextImageOfPreview() {
        this.copyBitmap = null
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
            applyEffect(captureRequest_Preview)
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

    @Throws(Exception::class)
    fun takePicture() {
        if(this.cameraDevice == null) {
            Log.e(TAG, "CameraDevice is null")
            return
        }
        if(this.imageReader == null) {
            Log.e(TAG, "Image Reader is null")
            return
        }

        // capture request pro snimek
        val captureBuilder: CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(imageReader!!.surface)
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        if(GlobalConfig.CAMERA_FLASH_MODE) {
            captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
        } else {
            captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        applyEffect(captureBuilder)

        cameraCaptureSessions?.stopRepeating();
        cameraCaptureSessions?.abortCaptures();
        val captureCallback: CaptureCallback = object : CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult,
            ) {
                Log.i(TAG, "Picture taken")
                updatePreview()
            }
        }
        cameraCaptureSessions?.capture(captureBuilder.build(), captureCallback, mBackgroundHandler)
        Log.i(TAG, "Capture request send")
    }

    /**********************************************************************************************/
    // PUBLIC SECTION END
    /**********************************************************************************************/

    private fun createCaptureSession() {
        try {
            //capture request pro priview
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequest_Preview = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequest_Preview!!.addTarget(surface)
            applyEffect(captureRequest_Preview)

            // vytvori capture session
            cameraDevice!!.createCaptureSession(listOf(surface, imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(context, "Failed to init Camera", Toast.LENGTH_SHORT).show()
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
        captureRequest_Preview!!.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(captureRequest_Preview!!.build(),
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

    @SuppressLint("MissingPermission")
    private fun openCamera(): Boolean {
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        if(manager == null) {
            Log.e(TAG, "CameraManager is NULL");
            return false
        }
        try {
            this.cameraId = manager.cameraIdList[0]
            if(cameraId == null) {
                Log.e(TAG, "Failed to open camera. CameraID is NULL");
                return false
            }
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

            // velikost preview
            imageDimension = chooseVideoSize(map.getOutputSizes(SurfaceTexture::class.java))
            Log.i(TAG, "PREVIEW SIZE: " + imageDimension!!.width.toString() + ", " + imageDimension!!.height.toString())

            // inicializace image readeru pro porizovani snimku
            val size = map.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }
            if (size != null) {
                imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 1)
                imageReader?.setOnImageAvailableListener({ reader ->
                    takePictureCallback?.invoke(reader.acquireLatestImage())
                }, mBackgroundHandler)
            }

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
            setAspectRatioTextureView(imageDimension!!.width, imageDimension!!.height)
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
            setAspectRatioTextureView(imageDimension!!.width, imageDimension!!.height)
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            // get the bitmap data of the TextureView
            if(copyBitmap != null) return
            val bitmap: Bitmap? = textureView!!.bitmap
            if (bitmap != null) {
                copyBitmap = bitmap.copy(bitmap.config, true)
                previewChangedCallback?.invoke(copyBitmap!!)
            }
        }
    }

    private var stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
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

    private fun chooseVideoSize(choices: Array<Size>): Size? {
        val smallEnough: MutableList<Size> = ArrayList()
        for (size in choices) {
            if (size.width == size.height * 4 / 3 && size.height <= 1080) {
                smallEnough.add(size)
            }
        }
        return if (smallEnough.size > 0) {
            Collections.max(smallEnough, CompareSizeByArea())
        } else choices[choices.size - 1]
    }

    private class CompareSizeByArea : Comparator<Size?> {
        override fun compare(lhs: Size?, rhs: Size?): Int {
            return java.lang.Long.signum(lhs!!.width.toLong() * lhs.height -
                    rhs!!.width.toLong() * rhs.height)
        }
    }

}