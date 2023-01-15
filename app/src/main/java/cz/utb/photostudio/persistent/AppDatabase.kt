package cz.utb.photostudio.persistent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageFile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun imageFileDao(): ImageFileDao

    companion object {
        var DATABASE: AppDatabase? = null

        fun initDatabase(context: Context) {
            AppDatabase.DATABASE = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "photostudio_db"
            ).build()
        }
    }

}