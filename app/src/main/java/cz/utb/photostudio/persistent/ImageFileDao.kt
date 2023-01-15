package cz.utb.photostudio.persistent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageFileDao {
    @Query("SELECT * FROM image_file")
    fun getAll(): List<ImageFile>

    @Query("SELECT * FROM image_file WHERE id == (:id)")
    fun loadByIds(id: Int): List<ImageFile>

    @Insert
    fun insertAll(vararg images: ImageFile)

    @Delete
    fun delete(images: ImageFile)
}