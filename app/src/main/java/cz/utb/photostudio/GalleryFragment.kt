package cz.utb.photostudio

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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

    val list = mutableListOf<ImageFile>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        val adapter = GalleryListAdapter(requireContext(), list)
        binding.recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        binding.recyclerView.adapter = adapter
        reloadList()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this.requireContext(), R.array.gallery_search_modes, R.layout.spinner_item)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun reloadList() {
        Executors.newSingleThreadExecutor().execute {
            try {
                val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                val l: List<ImageFile> = db.imageFileDao().getAll()
                with(list){
                    clear()
                    addAll(l)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}