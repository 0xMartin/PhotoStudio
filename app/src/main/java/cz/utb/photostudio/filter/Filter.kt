package cz.utb.photostudio.filter

import android.graphics.Bitmap
import androidx.fragment.app.Fragment

abstract class Filter {

    protected var changed: (()->Unit)? = null

    /**
     * Priradi filtru on changed callback (je vyvolan ve chvili kdy dojde k nejake zmene filtru)
     */
    fun setOnChangedCallback(changed: (()->Unit)?) {
        this.changed = changed
    }

    /**
     * Navrati fragment pro ovladani filtru
     */
    abstract fun getControllFragment(): Fragment?

    /**
     * Aplikuje filtr na bitmapu a navrati jeji upravenou podobu
     */
    abstract fun applyFilter(input: Bitmap): Bitmap?

    /**
     * Reset nastaveni filtru (povodni nastaveni)
     */
    abstract fun reset()

}