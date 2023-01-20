package cz.utb.photostudio.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="image_file")
data class ImageFile(
    @PrimaryKey(autoGenerate = true) var uid: Int,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "image") var imagePath: String
)