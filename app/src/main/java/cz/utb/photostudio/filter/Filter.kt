package cz.utb.photostudio.filter

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.fragment.app.Fragment

abstract class Filter(name: String) {

    var filter_Name: String = ""

    protected var changed: (()->Unit)? = null

    init {
        this.filter_Name = name
    }

    /**
     * Priradi filtru on changed callback (je vyvolan ve chvili kdy dojde k nejake zmene filtru)
     */
    fun setOnChangedCallback(changed: (()->Unit)?) {
        this.changed = changed
    }

    /**
     * Nastavi jmeno filtru
     */
    fun setFilterName(name: String) {
        this.filter_Name = name
    }

    /**
     * Navrati jmeno filtru
     */
    fun getFilterName(): String {
        return this.filter_Name
    }

    /**
     * Navrati fragment pro ovladani filtru
     */
    abstract fun getControllFragment(): Fragment?

    /**
     * Aplikuje filtr na bitmapu
     */
    abstract fun applyFilter(matrix: ColorMatrix)

    /**
     * Reset nastaveni filtru (povodni nastaveni)
     */
    abstract fun reset()

}
