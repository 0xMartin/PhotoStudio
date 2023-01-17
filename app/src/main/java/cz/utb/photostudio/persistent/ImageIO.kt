package cz.utb.photostudio.persistent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*


class ImageIO {
    companion object {
        fun saveImage(context: Context, image: Image): String {
            // image to bitmap
            val buffer: ByteBuffer = image.planes[0].buffer
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)

            // generate random name
            val uuid: UUID = UUID.randomUUID()
            val fileName = "image_$uuid.png"

            // ulozi obrazek
            val file = File(context.filesDir, fileName)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            Log.i("IMAGE_IO", "Image saved: ${file.path}")

            return fileName
        }

        fun loadImage(context: Context, path: String): Bitmap? {
            val file = File(context.filesDir, path)
            return if(file.exists()) {
                Log.i("IMAGE_IO", "Image loaded: ${file.path}")
                BitmapFactory.decodeFile(file.path);
            } else {
                null
            }
        }

        fun getPath(context: Context, path: String): String {
            return File(context.filesDir, path).path
        }

    }
}