package cz.utb.photostudio.util

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import cz.utb.photostudio.R
import cz.utb.photostudio.persistent.ImageFile
import cz.utb.photostudio.persistent.ImageIO


class GalleryListAdapter(context: Context, dataList: List<ImageFile>) : RecyclerView.Adapter<GalleryListAdapter.ViewHolder>() {
    private val dataList: List<ImageFile>
    private val context: Context

    init {
        this.context = context
        this.dataList = dataList
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val data: ImageFile = dataList[position]
        val bitmap: Bitmap? = ImageIO.loadImage(context, data.imagePath)
        if(bitmap != null) {
            holder.imageView.setImageBitmap(bitmap)
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

