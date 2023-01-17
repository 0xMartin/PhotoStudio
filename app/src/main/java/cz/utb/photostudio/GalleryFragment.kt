package cz.utb.photostudio

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import cz.utb.photostudio.databinding.FragmentGalleryBinding
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.util.GalleryListAdapter
import cz.utb.photostudio.util.getDatePickerDialog
import java.util.*
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this.context!!, R.array.gallery_search_modes, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinner.adapter = adapter

        // vyhledavani
        binding.buttonFind.setOnClickListener {

        }

        // vyber datumu
        binding.timeSelector.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val dpd: DatePickerDialog = getDatePickerDialog(this.context,
                { view, year, month, day ->
                    binding.timeSelector.text = "$day. $month. $year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        // odstraneni vsech dat (mazani je aplikovane na vyber zaznamu)
        binding.buttonDelete.setOnClickListener {

        }

        reloadList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun reloadList() {
        Executors.newSingleThreadExecutor().execute {
            try {
                val db: AppDatabase = AppDatabase.getDatabase(context!!)
                val list: List<ImageFile> = db.imageFileDao().getAll()
                val adapter = GalleryListAdapter(context!!, list)
                binding.recyclerView.adapter = adapter
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}