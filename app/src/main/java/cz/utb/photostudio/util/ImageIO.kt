package cz.utb.photostudio.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import cz.utb.photostudio.config.GlobalConfig
import cz.utb.photostudio.filter.Filter
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.FilterPersistentDao
import cz.utb.photostudio.persistent.ImageFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class ImageIO {
    companion object {

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        fun getPath(context: Context, path: String): String {
            return File(context.filesDir, path).path
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

        fun exportImageToGallery(context: Context, image: ImageFile, applyFilters: Boolean) {
            Executors.newSingleThreadExecutor().execute {
                try {
                    // load img from local file system
                    val bitmap: Bitmap? = loadImage(context, image.imagePath)

                    // load filters from db and apply tham on img
                    if(applyFilters) {
                        val db: AppDatabase = AppDatabase.getDatabase(context)
                        val filters: FilterPersistentDao = db.filterPersistentDao()

                        val matrix = ColorMatrix()
                        for(fp in filters.getAllWithImageUID(image!!.uid)) {
                            val f: Filter? = fp.createFilter()
                            f?.applyFilter(matrix)
                        }

                        // apply color matrix
                        val newBitmap = bitmap!!.copy(bitmap.config, true)
                        val canvas = Canvas(newBitmap)
                        val paint = Paint()
                        paint.colorFilter = ColorMatrixColorFilter(matrix)
                        canvas.drawBitmap(bitmap, 0f, 0f, paint)

                        // save image to gallery (with filters)
                        saveImageToGallery(context, newBitmap, "")
                    } else {
                        // save image to gallery (without filters)
                        bitmap?.let { saveImageToGallery(context, it, "") }
                    }

                } catch (e: java.lang.Exception) {
                    Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    })
                    e.printStackTrace()
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun saveImageToGallery(context: Context, bitmap: Bitmap, folderName: String) {
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),
                folderName
            )
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "IMG_$timeStamp.jpg"
            val image = File(storageDir, imageFileName)
            try {
                val fos = FileOutputStream(image)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
                MediaScannerConnection.scanFile(
                    context, arrayOf(image.toString()),
                    null,
                    null
                )
                // Display a message indicating that the image was saved
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    Toast.makeText(context, "Image exported successfully", Toast.LENGTH_SHORT)
                        .show()
                })
            } catch (e: IOException) {
                e.printStackTrace()
                // Display a message indicating that the image was not saved
                Handler(Looper.getMainLooper()).post(java.lang.Runnable {
                    Toast.makeText(context, "Failed to export image", Toast.LENGTH_SHORT).show()
                })
            }
        }

    }
}