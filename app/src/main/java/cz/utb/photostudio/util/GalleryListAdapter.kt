package cz.utb.photostudio.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cz.utb.photostudio.ImageFragment
import cz.utb.photostudio.R
import cz.utb.photostudio.persistent.ImageFile
import java.util.concurrent.Executors


class GalleryListAdapter(context: Context, dataList: List<ImageFile>) : RecyclerView.Adapter<GalleryListAdapter.ViewHolder>() {
    private val dataList: List<ImageFile>
    private val context: Context

    init {
        this.context = context
        this.dataList = dataList
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.gallery_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val data: ImageFile = dataList[position]
        Executors.newSingleThreadExecutor().execute {
            // nacte obrazek z uloziste
            val bitmap: Bitmap = ImageIO.loadImage(context, data.imagePath) ?: return@execute
            // scale obrazku na minimalni zobrazovaci velikost
            val wPerItem = context.resources.displayMetrics.widthPixels / 2 - 24
            val scaled = Bitmap.createScaledBitmap(bitmap, wPerItem,
                ((wPerItem.toFloat()/bitmap.width) * bitmap.height).toInt(), true)
            // obrazek
            Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                holder.initClickListener(data.uid)
                if (scaled != null) {
                    holder.imageView.setImageBitmap(scaled)
                }
            })
        }
    }

    fun removeAll() {
        val size: Int = dataList.size
        if (size > 0) {
            with(dataList as MutableList){
                clear()
            }
            notifyItemRangeRemoved(0, size)
        }
    }

    fun insertAll(list: List<ImageFile>)
    {
        for(img in list) {
            with(dataList as MutableList){
                add(img)
            }
            notifyItemInserted(dataList.size - 1)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        init {
            imageView = itemView.findViewById(R.id.imageView)
        }

        fun initClickListener(uid: Int) {
            imageView.setOnClickListener {
                val bundle = bundleOf(ImageFragment.ARG_IMG_UID to uid)
                itemView.findNavController().navigate(R.id.action_GalleryFragment_to_imageFragment, bundle)
            }
        }
    }

}

