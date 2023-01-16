package cz.utb.photostudio.persistent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageFileDao {
    @Query("SELECT COUNT(uid) FROM image_file")
    fun getCount(): Int

    @Query("SELECT * FROM image_file")
    fun getAll(): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE uid == (:uid)")
    fun getByIds(uid: Int): List<ImageFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: ImageFile)

    @Delete
    fun delete(images: ImageFile)

    @Query("DELETE FROM image_file")
    fun deleteAll()
}