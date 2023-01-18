package cz.utb.photostudio

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import cz.utb.photostudio.databinding.FragmentGalleryBinding
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.util.GalleryListAdapter
import cz.utb.photostudio.util.getDatePickerDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    private val list = mutableListOf<ImageFile>()

    private var galleryListAdapter: GalleryListAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        this.galleryListAdapter = GalleryListAdapter(requireContext(), list)
        binding.recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        binding.recyclerView.adapter = this.galleryListAdapter
        reloadList(null)

        return binding.root
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this.requireContext(), R.array.gallery_search_modes, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinner.adapter = adapter

        // vyhledavani
        binding.buttonFind.setOnClickListener {
            val date: LocalDateTime = LocalDateTime.parse(binding.timeSelector.text.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            reloadList(date)
            Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                binding.recyclerView.invalidate()
            })
        }

        // vyber datumu
        val current = LocalDateTime.now()
        binding.timeSelector.text = "${current.year}-${current.month.value}-${current.dayOfMonth}"
        binding.timeSelector.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val dpd: DatePickerDialog = getDatePickerDialog(this.context,
                { view, year, month, day ->
                    binding.timeSelector.text = "$year-$month-$day"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        // odstraneni vsech dat (mazani je aplikovane na vyber zaznamu)
        binding.buttonDelete.setOnClickListener {
            this.galleryListAdapter?.removeAll()
            Executors.newSingleThreadExecutor().execute {
                try {
                    val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                    db.imageFileDao().deleteAll()
                    reloadList(null)
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        binding.recyclerView.invalidate()
                    })
                } catch (e: java.lang.Exception) {
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        Toast.makeText(requireContext(), "Failed to delete images", Toast.LENGTH_SHORT).show()
                    })
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class DateSearch {
        BY_YEAR, BY_MONTH, BY_DAY
    }

    private fun reloadList(date: LocalDateTime?, search: DateSearch = DateSearch.BY_YEAR) {
        this.galleryListAdapter?.removeAll()
        Executors.newSingleThreadExecutor().execute {
            try {
                val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                if(date == null) {
                    list.addAll(db.imageFileDao().getAll())
                } else {
                    when(search) {
                        DateSearch.BY_YEAR -> {
                            list.addAll(db.imageFileDao().searchByYear(date.year))
                        }
                        DateSearch.BY_MONTH -> {
                            list.addAll(db.imageFileDao().searchByMonth(date.year, date.monthValue))
                        }
                        DateSearch.BY_DAY -> {
                            list.addAll(db.imageFileDao().searchByDay(date.year, date.monthValue, date.dayOfMonth))
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}