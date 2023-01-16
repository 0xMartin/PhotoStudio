package cz.utb.photostudio


import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import cz.utb.photostudio.databinding.FragmentCameraBinding
import cz.utb.photostudio.objectdetection.TensorFlowObjDetector
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.service.CameraService
import kotlinx.coroutines.*
import org.tensorflow.lite.task.vision.detector.Detection
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CameraFragment : Fragment(), TensorFlowObjDetector.DetectorListener {

    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var cameraService : CameraService = CameraService()

    private var objDetector: TensorFlowObjDetector? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        this._binding = FragmentCameraBinding.inflate(inflater, container, false)

        // inicializuje kamera servis
        this.context?.let {
            this.cameraService.initService(it, this.binding.textureView)
        }

        // spusti servis
        this.cameraService.startService()

        // inicializuje detekci objektu
        this.objDetector = TensorFlowObjDetector(
            context = requireContext(),
            objectDetectorListener = this)
        this.objDetector?.initObjectDetector(this.binding.textureView)

        // spusti detektor objektu
        this.objDetector?.runDetector(5)

        return this.binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // animace hlavniho tlacitka
        val colorFrom = ContextCompat.getColor(this.context!!, R.color.main_color_transparent)
        val colorTo = ContextCompat.getColor(this.context!!, R.color.main_color)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorFrom)
        colorAnimation.duration = 2500 // milliseconds
        colorAnimation.repeatCount = ValueAnimator.INFINITE
        colorAnimation.addUpdateListener {
                animator -> binding.buttonCapture.backgroundTintList = ColorStateList.valueOf(animator.animatedValue as Int)
        }
        colorAnimation.start()

        // udela snimek
        this.binding.buttonCapture.setOnClickListener {
            try {
                this.cameraService.takePicture { image ->
                    Log.i("CAMEAR", "Picture taken")
                    Log.i("INFO", image.width.toString() + ", " + image.height.toString())
                    Toast.makeText(requireContext(), "Picture taken", Toast.LENGTH_SHORT).show()
                    // ulozeni do lokalni db
                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val db: AppDatabase = AppDatabase.getDatabase(context!!)
                            val buffer: ByteBuffer = image.planes[0].buffer
                            val img = ImageFile(
                                db.imageFileDao().getCount(),
                                "now",
                                buffer.array()
                            )
                            db.imageFileDao().insert(img)
                            // close img
                            image.close()
                        }catch (ex: java.lang.Exception) {
                            // close img
                            image.close()
                        }
                    }
                }
            }catch (ex: java.lang.Exception) {
                Toast.makeText(requireContext(), "Picture take error", Toast.LENGTH_SHORT).show()
            }
        }

        // vyber efektu
        this.binding.fxNormal.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_OFF)
            selectFx(this.binding.fxNormal)
        }
        this.binding.fxAqua.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_AQUA)
            selectFx(this.binding.fxAqua)
        }
        this.binding.fxBlackboard.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD)
            selectFx(this.binding.fxBlackboard)
        }
        this.binding.fxMono.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_MONO)
            selectFx(this.binding.fxMono)
        }
        this.binding.fxNegative.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE)
            selectFx(this.binding.fxNegative)
        }
        this.binding.fxPosterize.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE)
            selectFx(this.binding.fxPosterize)
        }
        this.binding.fxSepia.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_SEPIA)
            selectFx(this.binding.fxSepia)
        }
        this.binding.fxSolarize.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE)
            selectFx(this.binding.fxSolarize)
        }
        this.binding.fxWhiteboard.setOnClickListener {
            this.cameraService.selectEffect(CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD)
            selectFx(this.binding.fxWhiteboard)
        }

        // default fx
        selectFx(this.binding.fxNormal)
    }

    override fun onDestroyView() {
        this.cameraService.stopService()
        super.onDestroyView()
        this._binding = null
    }

    override fun onResume() {
        super.onResume()
        this.cameraService.startService()
    }

    override fun onPause() {
        this.cameraService.stopService()
        super.onPause()
    }

    private fun selectFx(textView: TextView) {
        this.binding.fxNormal.setTextColor(Color.WHITE)
        this.binding.fxAqua.setTextColor(Color.WHITE)
        this.binding.fxBlackboard.setTextColor(Color.WHITE)
        this.binding.fxMono.setTextColor(Color.WHITE)
        this.binding.fxNegative.setTextColor(Color.WHITE)
        this.binding.fxPosterize.setTextColor(Color.WHITE)
        this.binding.fxSepia.setTextColor(Color.WHITE)
        this.binding.fxSolarize.setTextColor(Color.WHITE)
        this.binding.fxWhiteboard.setTextColor(Color.WHITE)
        this.context?.let {
            textView.setTextColor(ContextCompat.getColor(it, R.color.main_color))
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        this.activity?.runOnUiThread {
            // Pass necessary information to OverlayView for drawing on the canvas
            this.binding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )
            // Force a redraw
            this.binding.overlay.invalidate()
        }
    }

}