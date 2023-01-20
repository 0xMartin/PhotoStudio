package cz.utb.photostudio.persistent

import androidx.room.*

@Dao
interface FilterPersistentDao {

    @Query("SELECT COUNT(id) FROM imageFilter")
    fun getCount(): Int

    @Query("SELECT * FROM imageFilter")
    fun getAll(): List<FilterPersistent>

    @Query("SELECT * FROM imageFilter WHERE id == (:id)")
    fun getByID(id: Int): FilterPersistent

    @Query("SELECT * FROM imageFilter WHERE image_uid == (:uid)")
    fun getAllWithImageUID(uid: Int): List<FilterPersistent>

    @Query("SELECT * FROM imageFilter WHERE name == (:name)")
    fun getByName(name: String): FilterPersistent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filterProp: FilterPersistent)

    @Delete
    fun delete(filterProp: FilterPersistent)

    @Query("DELETE FROM imageFilter")
    fun deleteAll()

    @Query("DELETE FROM imageFilter WHERE image_uid == (:uid)")
    fun deleteAllWithImageUID(uid: Int)

}