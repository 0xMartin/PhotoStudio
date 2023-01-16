package cz.utb.photostudio.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="image_file")
data class ImageFile(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "image") val imagePath: String
)