package cz.utb.photostudio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.utb.photostudio.camera.CameraService
import cz.utb.photostudio.databinding.FragmentCameraBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null

    private var cameraService : CameraService = CameraService()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this._binding = FragmentCameraBinding.inflate(inflater, container, false)

        // inicializuje kamera servis
        this.context?.let {
            this.cameraService.initService(it, this.binding.textureView)
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // udela snimek
        this.binding.buttonCapture.setOnClickListener {
            this.cameraService.takePicture();
        }
    }

    override fun onDestroyView() {
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

}