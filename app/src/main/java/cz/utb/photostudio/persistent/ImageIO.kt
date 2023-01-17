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
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            // generate random name
            val uuid: UUID = UUID.randomUUID()
            val fileName = "image_$uuid.jpg"

            // ulozi obrazek
            val file = File(context.filesDir, fileName)
            val out = FileOutputStream(file)
            out.write(bytes)
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