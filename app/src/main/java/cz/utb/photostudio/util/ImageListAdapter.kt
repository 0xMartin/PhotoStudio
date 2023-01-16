package cz.utb.photostudio.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cz.utb.photostudio.R
import cz.utb.photostudio.persistent.ImageFile


class ImageListAdapter(private val context: Context, private val imageList: List<ImageFile>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = null

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false)
        }

        try {
            val current: ImageFile = imageList[position]
            val image: Bitmap = BitmapFactory.decodeByteArray(current.image, 0, current.image.size)

            val textureView: TextureView? = view?.findViewById(R.id.gallery_item_texture)

            val canvas: Canvas? = textureView?.lockCanvas()
            val rect: Rect = Rect(0, 0, image.width, image.height)
            canvas?.drawBitmap(image, rect, rect, null);

            canvas?.let { textureView.unlockCanvasAndPost(it) };
        } catch (_ : java.lang.Exception) {}

        return view!!
    }

    override fun getItem(position: Int): Any {
        return imageList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return imageList.size
    }

}