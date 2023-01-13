package cz.utb.photostudio

import android.hardware.Camera
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import cz.utb.photostudio.camera.CameraPreview
import cz.utb.photostudio.camera.CameraService
import cz.utb.photostudio.databinding.FragmentCameraBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null

    private var camera: Camera? = null
    private var cameraPreview: CameraPreview? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this._binding = FragmentCameraBinding.inflate(inflater, container, false)

        // Create an instance of Camera
        this.camera = CameraService.getCameraInstance()

        this.cameraPreview = this.camera?.let {
            // Create our Preview view
            this.context?.let { cntx -> CameraPreview(cntx, it) }
        }

        // Set the Preview view as the content of our activity.
        this.cameraPreview?.also {
            val preview: FrameLayout = this.binding.cameraPreview
            preview.addView(it)
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.buttonCapture.setOnClickListener {
            // get an image from the camera
            this.camera?.takePicture(null, null, CameraService.defaultPictureCallback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}