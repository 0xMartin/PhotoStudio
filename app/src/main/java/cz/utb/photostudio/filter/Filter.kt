package cz.utb.photostudio.filter

import android.graphics.Bitmap
import androidx.fragment.app.Fragment

abstract class Filter {

    protected var changed: (()->Unit)? = null

    fun setChangedCallback(changed: (()->Unit)?) {
        this.changed = changed
    }

    abstract fun getControllFragment(): Fragment?

    abstract fun applyFilter(input: Bitmap): Bitmap?

    abstract fun reset()

}