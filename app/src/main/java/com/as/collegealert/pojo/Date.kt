package com.`as`.collegealert.pojo

import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
import com.`as`.collegealert.R
import com.`as`.collegealert.ui.AddItemActivity
import java.util.Calendar

data class Date(val context: Context){
    var selectedDate = ""
        fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                if (context is AddItemActivity){
                    val date = context.findViewById<TextView>(R.id.selectedDateText)
                    date.text = selectedDate
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

}