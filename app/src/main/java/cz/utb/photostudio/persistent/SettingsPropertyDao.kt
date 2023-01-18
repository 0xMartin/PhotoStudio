package cz.utb.photostudio.persistent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsPropertyDao {
    @Query("SELECT COUNT(name) FROM settings")
    fun getCount(): Int

    @Query("SELECT * FROM settings")
    fun getAll(): List<SettingsProperty>

    @Query("SELECT * FROM settings WHERE name == (:id)")
    fun getByName(id: Int): SettingsProperty

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(settingsProperty: SettingsProperty)

    @Delete
    fun delete(settingsProperty: SettingsProperty)

    @Query("DELETE FROM settings")
    fun deleteAll()
}