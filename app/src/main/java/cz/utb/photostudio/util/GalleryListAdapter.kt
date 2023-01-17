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
import androidx.recyclerview.widget.RecyclerView
import cz.utb.photostudio.R
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.persistent.ImageIO
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
            val bitmap: Bitmap = ImageIO.loadImage(context, data.imagePath) ?: return@execute
            val wPerItem = context.resources.displayMetrics.widthPixels / 2 - 24
            val scaled = Bitmap.createScaledBitmap(bitmap, wPerItem,
                ((wPerItem.toFloat()/bitmap.width) * bitmap.height).toInt(), true)
            Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                if (scaled != null) {
                    holder.imageView.setImageBitmap(scaled)
                }
            })
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
    }

}

