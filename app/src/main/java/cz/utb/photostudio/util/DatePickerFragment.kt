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
    val dpd = DatePickerDialog(context!!, listener, year, month, day)
    dpd.datePicker.maxDate = System.currentTimeMillis() + 1000
    return dpd
}