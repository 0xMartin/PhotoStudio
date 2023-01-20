package cz.utb.photostudio.persistent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageFile::class, SettingsProperty::class, FilterPersistent::class], version = 6, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun imageFileDao(): ImageFileDao

    abstract fun filterPersistentDao(): FilterPersistentDao

    abstract fun settingsPropertyDao(): SettingsPropertyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
        ): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photostudio_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}