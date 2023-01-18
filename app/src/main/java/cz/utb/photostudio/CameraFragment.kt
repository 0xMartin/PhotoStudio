package cz.utb.photostudio


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import cz.utb.photostudio.databinding.FragmentCameraBinding
import cz.utb.photostudio.object_detection.TensorFlowObjDetector
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.service.CameraService
import cz.utb.photostudio.util.ImageIO
import kotlinx.coroutines.*
import org.tensorflow.lite.task.vision.detector.Detection
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

    // kamera servis
    private var cameraService : CameraService = CameraService()

    // detektor objektu
    private var objDetector: TensorFlowObjDetector? = null

    // animace barev tlacitka "take picture"
    var colorAnimation: ValueAnimator? = null


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
        this.cameraService.setPictureTakeCallback { img -> this.onPictureTakeEvent(img) }

        // inicializuje detekci objektu
        this.objDetector = TensorFlowObjDetector(
            context = requireContext(),
            objectDetectorListener = this)
        this.objDetector?.initObjectDetector(this.binding.textureView)

        // spusti detektor objektu
        this.objDetector?.runDetector(5)

        return this.binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // animace hlavniho tlacitka
        val colorFrom = ContextCompat.getColor(this.requireContext(), R.color.main_color_transparent)
        val colorTo = ContextCompat.getColor(this.requireContext(), R.color.main_color)
        this.colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorFrom)
        this.colorAnimation?.duration = 2500 // milliseconds
        this.colorAnimation?.repeatCount = ValueAnimator.INFINITE
        this.colorAnimation?.addUpdateListener {
                animator -> binding.buttonCapture.backgroundTintList = ColorStateList.valueOf(animator.animatedValue as Int)
        }
        this.colorAnimation?.start()

        // tlacitko galerie
        this.binding.buttonGallery.setOnClickListener {
            this.findNavController().navigate(R.id.From_Camera_To_Gallery)
        }

        // tlacitko editoru
        this.binding.buttonEdit.setOnClickListener {

        }

        // udela snimek
        this.binding.buttonCapture.setOnClickListener {
            try {
                this.cameraService.takePicture()
            }catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "Failed to take picture", Toast.LENGTH_SHORT).show()
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
        this.colorAnimation?.pause()
        this.cameraService.stopService()
        super.onDestroyView()
        this._binding = null
    }

    override fun onResume() {
        super.onResume()
        this.colorAnimation?.start()
        this.cameraService.startService()
    }

    override fun onPause() {
        this.colorAnimation?.pause()
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
            try {
                this.binding.overlay.setResults(
                    results ?: LinkedList<Detection>(),
                    imageHeight,
                    imageWidth
                )
                // Force a redraw
                this.binding.overlay.invalidate()
            } catch (_ : java.lang.Exception) {}
        }
    }

    private fun onPictureTakeEvent(image: Image) {
        Toast.makeText(context,
            "Picture taken",
            Toast.LENGTH_SHORT).show()
        Executors.newSingleThreadExecutor().execute {
            try {
                // oprazek ulozi na uloziste zarizeni
                val rotation: Int = this.activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
                val path: String = ImageIO.saveImage(requireContext(), image, rotation)
                // cas
                val calendar = Calendar.getInstance()
                calendar.toString()
                var current = calendar.get(Calendar.YEAR).toString() + "-"
                current += calendar.get(Calendar.MONTH).toString() + "-"
                current += calendar.get(Calendar.DAY_OF_MONTH).toString() + " "
                current += calendar.get(Calendar.HOUR).toString() + ":"
                current += calendar.get(Calendar.MINUTE).toString() + ":"
                current += calendar.get(Calendar.SECOND).toString()
                // ulozeni informaci do lokalni databaze
                val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                val img = ImageFile(
                    db.imageFileDao().getMaxUid() + 1,
                    current,
                    path
                )
                db.imageFileDao().insert(img)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    Toast.makeText(context,
                        "Failed to save picture",
                        Toast.LENGTH_SHORT).show()
                })
            }
            // close img
            image.close()
        }
    }

}