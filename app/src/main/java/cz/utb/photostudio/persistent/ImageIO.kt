package cz.utb.photostudio.persistent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import cz.utb.photostudio.config.GlobalConfig
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*


class ImageIO {
    companion object {

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        fun saveImage(context: Context, image: Image, rotation: Int): String {
            // image to bitmap
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size);
            val matrix = Matrix()
            matrix.postRotate(ORIENTATIONS.get(rotation).toFloat())
            val rotatedBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // generate random name
            val uuid: UUID = UUID.randomUUID()
            val fileName = "image_$uuid.jpg"

            // ulozi obrazek
            val file = File(context.filesDir, fileName)
            val out = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, GlobalConfig.PICTURE_QUALITY, out)
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