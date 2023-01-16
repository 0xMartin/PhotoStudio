package cz.utb.photostudio.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="image_file")
data class ImageFile(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB) val image: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageFile

        if (uid != other.uid) return false
        if (date != other.date) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid
        result = 31 * result + date.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}