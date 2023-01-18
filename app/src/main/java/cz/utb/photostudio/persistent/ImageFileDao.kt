package cz.utb.photostudio.persistent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageFileDao {
    @Query("SELECT COUNT(uid) FROM image_file")
    fun getCount(): Int

    @Query("SELECT COALESCE(MAX(uid), 0) FROM image_file")
    fun getMaxUid(): Int

    @Query("SELECT * FROM image_file ORDER BY uid DESC")
    fun getAll(): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE CAST(strftime('%Y', date) as decimal) == (:year) ORDER BY uid DESC")
    fun searchByYear(year: Int): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE CAST(strftime('%Y', date) as decimal) == (:year) AND CAST(strftime('%m', date) as decimal) == (:month) ORDER BY uid DESC")
    fun searchByMonth(year:Int, month: Int): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE CAST(strftime('%Y', date) as decimal) == (:year) AND CAST(strftime('%m', date) as decimal) == (:month) AND CAST(strftime('%d', date) as decimal) == (:day) ORDER BY uid DESC")
    fun searchByDay(year:Int, month: Int, day: Int): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE uid == (:uid)")
    fun getById(uid: Int): ImageFile

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: ImageFile)

    @Delete
    fun delete(images: ImageFile)

    @Query("DELETE FROM image_file")
    fun deleteAll()
}