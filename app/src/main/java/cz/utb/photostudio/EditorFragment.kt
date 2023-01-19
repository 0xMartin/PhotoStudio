package cz.utb.photostudio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import cz.utb.photostudio.databinding.FragmentEditorBinding
import cz.utb.photostudio.filter.Contrast


class EditorFragment : Fragment() {

    companion object {
        const val ARG_IMG_UID = "image_uid"
    }

    private var _binding: FragmentEditorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var img_uid: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            img_uid = it.getInt(ImageFragment.ARG_IMG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filter: Contrast = Contrast()
        realoadFxControllView(filter.getControllFragment()!!)
    }

    @Suppress("DEPRECATION")
    private fun realoadFxControllView(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager?.beginTransaction() ?: return

        fragmentTransaction.replace(R.id.fragment_container_view, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}