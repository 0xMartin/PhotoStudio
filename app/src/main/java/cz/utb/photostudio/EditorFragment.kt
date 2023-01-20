package cz.utb.photostudio

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import cz.utb.photostudio.databinding.FragmentEditorBinding
import cz.utb.photostudio.filter.Filter
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.FilterPersistent
import cz.utb.photostudio.persistent.FilterPersistentDao
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.util.FilterListAdapter
import cz.utb.photostudio.util.FilterSelectDialog
import cz.utb.photostudio.util.ImageIO
import java.util.*
import java.util.concurrent.Executors


class EditorFragment : Fragment() {

    companion object {
        const val ARG_IMG_UID = "image_uid"
    }

    private var _binding: FragmentEditorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // list filtru
    private var filterListAdapter: FilterListAdapter? = null
    private var selectedFilter: Filter? = null

    // obrazek se kterym se aktualne pracuje
    var image: ImageFile? = null
    var defaultBitmap: Bitmap? = null

    // dialog pro pridani filtru
    var filterSelectDialog: FilterSelectDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val img_uid = it.getInt(ImageFragment.ARG_IMG_UID)
            loadImageFromDB(img_uid)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)

        // list adapter pro filtry
        this.filterListAdapter = FilterListAdapter(requireContext(), LinkedList<Filter>())
        this.filterListAdapter?.setOnChangedCallback { filter->
            this.selectFilter(filter)
        }

        // recycler view pro list s filtry
        val mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recyclerViewFilters.layoutManager = mLayoutManager
        binding.recyclerViewFilters.adapter = this.filterListAdapter

        // dialog pro vytvoreni filtru
        this.filterSelectDialog =  FilterSelectDialog(this.activity)
        this.filterSelectDialog?.setOnSelectCallBack { filter ->
            filter?.let { this.addFilter(it, true) }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // prida novy filtr
        binding.buttonAdd.setOnClickListener {
            this.filterSelectDialog?.showDialog()
        }

        // odstrani aktualne vybrany filtr
        binding.buttonDelete.setOnClickListener {
            this.selectedFilter?.let { it1 -> this.removeFilter(it1) }
        }

        // ulozi obrazek do galerie i s filtrama
        binding.buttonExport.setOnClickListener {
            this.image?.let { it1 -> ImageIO.exportImageToGallery(requireContext(), it1, true) }
        }

        // ulozi filtry obrazku do databaze
        binding.buttonSave.setOnClickListener {
            Executors.newSingleThreadExecutor().execute {
                try {
                    val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                    val filtersDao: FilterPersistentDao = db.filterPersistentDao()
                    // delete all filters
                    filtersDao.deleteAllWithImageUID(this.image!!.uid)
                    // insert again
                    for(filter in this.filterListAdapter!!.getFilterList()) {
                        val fp: FilterPersistent = FilterPersistent.fromFilter(filter, this.image!!.uid)
                        filtersDao.insert(fp)
                    }

                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        Toast.makeText(requireContext(), "Filters saved successfully", Toast.LENGTH_SHORT).show()
                    })
                } catch (e: java.lang.Exception) {
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        Toast.makeText(requireContext(), "Failed to save filters", Toast.LENGTH_SHORT).show()
                    })
                    e.printStackTrace()
                }
            }
        }

    }

    @Suppress("DEPRECATION")
    private fun reloadFxControlView(fragment: Fragment?) {
        val fragmentTransaction: FragmentTransaction = fragmentManager?.beginTransaction() ?: return

        if(fragment != null) {
            fragmentTransaction.replace(R.id.fragment_container_view, fragment)
        } else {
            fragmentTransaction.replace(R.id.fragment_container_view, EmptyFragment())
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun loadImageFromDB(img_uid: Int) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val db: AppDatabase = AppDatabase.getDatabase(requireContext())
                // load image data
                image = db.imageFileDao().getById(img_uid)
                defaultBitmap = ImageIO.loadImage(requireContext(), image!!.imagePath)

                // load & parse filters
                val filterList = LinkedList<Filter>()
                val filtersDao: FilterPersistentDao = db.filterPersistentDao()
                for(fp in filtersDao.getAllWithImageUID(image!!.uid)) {
                    val f: Filter? = fp.createFilter()
                    f?.let { it -> filterList.add(it) }
                }

                // reload image preview and filter list
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    // add all filters
                    for(f in filterList) {
                        addFilter(f, false)
                    }
                    // reload image preview
                    reloadImagePreview()
                })
            } catch (e: java.lang.Exception) {
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                })
                e.printStackTrace()
            }
        }
    }

    private fun reloadImagePreview() {
        if(this.defaultBitmap == null) return

        if (defaultBitmap!!.isRecycled) {
            Log.e("EDITOR", "Default bitmap was recycled!!")
            return
        }

        if(this.filterListAdapter != null){
            val matrix = ColorMatrix()

            for(filter in this.filterListAdapter!!.getFilterList()) {
                filter.applyFilter(matrix)
            }

            val newBitmap = this.defaultBitmap!!.copy(this.defaultBitmap!!.config, true)
            val canvas = Canvas(newBitmap)
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            canvas.drawBitmap(this.defaultBitmap!!, 0f, 0f, paint)

            binding.imagePreview.setImageBitmap(newBitmap)
        } else {
            binding.imagePreview.setImageBitmap(this.defaultBitmap!!)
        }
    }

    private fun selectFilter(filter: Filter?) {
        this.selectedFilter = filter
        if(this.selectedFilter != null){
            this.reloadFxControlView(this.selectedFilter!!.getControllFragment()!!)
        } else {
            this.reloadFxControlView(null)
        }
    }

    private fun addFilter(filter: Filter, addIndex: Boolean) {
        if(addIndex) {
            var cnt: Int = 1
            for(f in this.filterListAdapter?.getFilterList()!!) {
                if(f::class == filter::class) {
                    cnt++
                }
            }
            filter.filter_Name += " $cnt"
        }
        this.filterListAdapter?.addFilter(filter)
        filter.setOnChangedCallback {
            this.reloadImagePreview()
        }
        this.selectFilter(filter)
        this.reloadImagePreview()
    }

    private fun removeFilter(filter: Filter) {
        this.filterListAdapter?.removeFilter(filter)
        if(this.selectedFilter == filter) {
            this.selectFilter(null)
        }
        this.reloadImagePreview()
    }

    class EmptyFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            return inflater.inflate(R.layout.fragment_empty, container, false)
        }
    }
    
}