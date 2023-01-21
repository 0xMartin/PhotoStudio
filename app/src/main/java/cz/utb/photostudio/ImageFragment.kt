package cz.utb.photostudio

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cz.utb.photostudio.databinding.FragmentImageBinding
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.util.ImageIO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors


class ImageFragment : Fragment() {

    companion object {
        const val ARG_IMG_UID = "image_uid"
    }

    private var _binding: FragmentImageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var img_uid: Int? = null
    private var image: ImageFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            img_uid = it.getInt(ARG_IMG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // spusti editor pro tento obrazek
        binding.buttonEdit.setOnClickListener {
            val bundle = bundleOf(EditorFragment.ARG_IMG_UID to this.img_uid)
            findNavController().navigate(R.id.action_imageFragment_to_editorFragment, bundle)
        }

        // exportuje obrazek do galerie
        binding.buttonExport.setOnClickListener {
            this.image?.let { it1 -> ImageIO.exportImageToGallery(requireContext(), it1, null) }
        }

        // odstraneni tohoto obrazku
        binding.buttonDelete.setOnClickListener {
            Executors.newSingleThreadExecutor().execute {
                try {
                    val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                    val image: ImageFile = img_uid?.let { db.imageFileDao().getById(it) } ?: return@execute
                    db.filterPersistentDao().deleteAllWithImageUID(image.uid)
                    db.imageFileDao().delete(image)
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        findNavController().popBackStack()
                    })
                } catch (e: java.lang.Exception) {
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        Toast.makeText(requireContext(), "Failed to remove image", Toast.LENGTH_SHORT).show()
                    })
                    e.printStackTrace()
                }
            }
        }

        // nacte data o obrazku
        Executors.newSingleThreadExecutor().execute {
            try {
                val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                image = img_uid?.let { db.imageFileDao().getById(it) } ?: return@execute
                val bitmap: Bitmap? = ImageIO.loadImage(requireContext(), image!!.imagePath)
                bitmap?.let {
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        try {
                            val date: LocalDateTime = LocalDateTime.parse(image!!.date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            binding.textView.text = "${date.dayOfMonth}. ${date.monthValue}. ${date.year} - " +
                                    "${"%02d".format(date.hour)}:${"%02d".format(date.minute)}:${"%02d".format(date.second)}"
                            binding.imageView.setImageBitmap(bitmap)
                        } catch (_: java.lang.Exception) {}
                    })
                }
            } catch (e: java.lang.Exception) {
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                })
                e.printStackTrace()
            }
        }
    }

}