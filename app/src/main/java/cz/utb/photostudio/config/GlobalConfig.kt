package cz.utb.photostudio.config

import android.content.Context
import cz.utb.photostudio.persistent.AppDatabase
import cz.utb.photostudio.persistent.SettingsProperty
import java.util.*
import java.util.concurrent.Executors


class GlobalConfig {
    companion object {
        private val KEY_OBJ_DETECTION_ENABLED: String = "OBJ_DETECTION_ENABLED"
        var OBJ_DETECTION_ENABLED: Boolean = false

        private val KEY_CAMERA_FLASH_MODE: String = "CAMERA_FLASH_MODE"
        var CAMERA_FLASH_MODE: Boolean = false

        private val KEY_PICTURE_QUALITY: String = "PICTURE_QUALITY"
        var PICTURE_QUALITY: Int = 75

        fun loadSetting(context: Context) {
            Executors.newSingleThreadExecutor().execute {
                try {
                    val db: AppDatabase = AppDatabase.getDatabase(context)
                    for (settingsProperty in db.settingsPropertyDao().getAll()) {
                        val value: String = settingsProperty.value.lowercase(Locale.getDefault())
                        when(settingsProperty.name) {
                            KEY_OBJ_DETECTION_ENABLED -> {
                                OBJ_DETECTION_ENABLED = value == "true"
                            }
                            KEY_CAMERA_FLASH_MODE -> {
                                CAMERA_FLASH_MODE = value == "true"
                            }
                            KEY_PICTURE_QUALITY -> {
                                PICTURE_QUALITY = value.toInt()
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun storeSettings(context: Context) {
            Executors.newSingleThreadExecutor().execute {
                try {
                    val db: AppDatabase = AppDatabase.getDatabase(context)
                    val settingsDao = db.settingsPropertyDao()
                    settingsDao.insert(SettingsProperty(KEY_OBJ_DETECTION_ENABLED, if(OBJ_DETECTION_ENABLED) "true" else "false"))
                    settingsDao.insert(SettingsProperty(KEY_CAMERA_FLASH_MODE, if(CAMERA_FLASH_MODE) "true" else "false"))
                    settingsDao.insert(SettingsProperty(KEY_PICTURE_QUALITY, PICTURE_QUALITY.toString()))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}