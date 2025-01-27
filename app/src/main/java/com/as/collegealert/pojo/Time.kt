package com.`as`.collegealert.pojo

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TextView
import com.`as`.collegealert.R
import com.`as`.collegealert.ui.AddItemActivity
import java.util.Calendar

data class Time(val context: Context) {
    var selectedTime = ""
    fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (context is AddItemActivity){
                    val time = context.findViewById<TextView>(R.id.selectedTimeText)
                    time.text = selectedTime
                }
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }
}