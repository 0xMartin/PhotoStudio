package cz.utb.photostudio.util

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context


fun getDatePickerDialog(
    context: Context?,
    listener: OnDateSetListener?,
    year: Int,
    month: Int,
    day: Int,
): DatePickerDialog {
    return DatePickerDialog(context!!, listener, year, month, day)
}