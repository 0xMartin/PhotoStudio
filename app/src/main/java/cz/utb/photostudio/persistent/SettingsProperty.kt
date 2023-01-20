package cz.utb.photostudio.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="settings")
data class SettingsProperty(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "value")
    var value: String
)