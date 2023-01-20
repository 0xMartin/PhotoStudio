package cz.utb.photostudio.util

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import cz.utb.photostudio.filter.*


class FilterSelectDialog(activity: Activity?) {

    private var onSelectCallBack: ((filter: Filter?)->Unit)? = null

    private var selectedFilter: String? = null

    var alertDialog: AlertDialog? = null

    init {
        this.createDialog(activity)
    }

    private fun createDialog(activity: Activity?) {
        this.alertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Select Filter")
            builder.apply {
                setItems(arrayOf("Contrast", "Brightness", "Saturation", "RGB"),
                    DialogInterface.OnClickListener { dialog, which ->
                        when(which) {
                            0 -> onSelectCallBack?.invoke(Contrast("Contrast"))
                            1 -> onSelectCallBack?.invoke(Brightness("Brightness"))
                            2 -> onSelectCallBack?.invoke(Saturation("Saturation"))
                            3 -> onSelectCallBack?.invoke(RGB("RGB"))
                        }
                    })
                setNegativeButton("Cancle",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }

            builder.create()
        }
    }

    fun showDialog() {
        this.alertDialog?.show()
    }

    fun setOnSelectCallBack(callback: ((filter: Filter?)->Unit)?) {
        this.onSelectCallBack = callback
    }

}