package cz.utb.photostudio.persistent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
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
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            // gerate random name
            val uuid: UUID = UUID.randomUUID()
            val fileName = "image_$uuid.png"

            // ulozi obrazek
            val file = File(context.filesDir, fileName)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            return fileName
        }

        fun loadImage(context: Context, path: String): Bitmap{
            val file = File(context.filesDir, path)
            return BitmapFactory.decodeFile(file.path);
        }
    }
}