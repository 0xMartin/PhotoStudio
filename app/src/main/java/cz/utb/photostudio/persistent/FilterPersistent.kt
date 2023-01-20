package cz.utb.photostudio.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.utb.photostudio.filter.*

@Entity(tableName="imageFilter")
data class FilterPersistent(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "image_uid") var image_uid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "param1") var param1: String,
    @ColumnInfo(name = "param2") var param2: String,
    @ColumnInfo(name = "param3") var param3: String,
    @ColumnInfo(name = "param4") var param4: String,
    @ColumnInfo(name = "param5") var param5: String
) {

    companion object {

        const val CONTRAST_TYPE: String = "CONTRAST"
        const val BRIGHTNESS_TYPE: String = "BRIGHTNESS"
        const val SATURATION_TYPE: String = "SATURATION"
        const val RGB_TYPE: String = "RGB"

        fun fromFilter(filter: Filter, image_uid: Int): FilterPersistent {
            val ret = FilterPersistent(0, image_uid, filter.filter_Name,"","","","","","")
            when (filter) {
                is Contrast -> {
                    ret.type = CONTRAST_TYPE
                    ret.param1 = (filter as Contrast).getContrast().toString()
                }
                is Brightness -> {
                    ret.type = BRIGHTNESS_TYPE
                    ret.param1 = (filter as Brightness).getBrightness().toString()
                }
                is Saturation -> {
                    ret.type = SATURATION_TYPE
                    ret.param1 = (filter as Saturation).getSaturation().toString()
                }
                is RGB -> {
                    ret.type = RGB_TYPE
                    ret.param1 = (filter as RGB).getRed().toString()
                    ret.param2 = (filter as RGB).getGreen().toString()
                    ret.param3 = (filter as RGB).getBlue().toString()
                }
            }
            return ret
        }
    }

    fun createFilter(): Filter? {
        when (this.type) {
            CONTRAST_TYPE -> {
                return Contrast(this.name, this.param1.toFloat())
            }
            BRIGHTNESS_TYPE -> {
                return Brightness(this.name, this.param1.toFloat())
            }
            SATURATION_TYPE -> {
                return Saturation(this.name, this.param1.toFloat())
            }
            RGB_TYPE -> {
                return RGB(this.name, this.param1.toFloat(), this.param2.toFloat(), this.param3.toFloat())
            }
        }
        return null
    }

}