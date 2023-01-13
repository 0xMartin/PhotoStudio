package cz.utb.photostudio

import android.hardware.Camera
import android.os.Bundle
import android.util.Log
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

        // overi zda ma zarizeni kameru
        this.context?.let { it1 ->
            {
                if(!CameraService.checkCameraHardware(it1)) {
                    Log.d("CAMERA_FRAGMENT", "Error camera not found")
                }
            }
        }

        // ziska instanci kamery
        this.camera = CameraService.getCameraInstance()

        // vytvori preview pro obraz zaznamenavany z kamery
        this.cameraPreview = this.camera?.let {
            this.context?.let { it1 -> CameraPreview(it1, it) }
        }

        // preview nastavi jako obsah frame layoutu
        this.cameraPreview?.also {
            val preview: FrameLayout = this.binding.cameraPreview
            preview.addView(it)
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // udela snimek
        this.binding.buttonCapture.setOnClickListener {
            this.camera?.takePicture(null, null, CameraService.defaultPictureCallback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}