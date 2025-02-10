package com.example.fleetapp.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun DateRangePicker(
    onDateRangeSelected: (String, String) -> Unit,
) {
    val context = LocalContext.current

    val dateFormat = SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault())

    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    val startDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            startDate = dateFormat.format(calendar.time)
            onDateRangeSelected(startDate, endDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        if (endDate.isNotEmpty()) {
            val endCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(endDate) ?: Date()
            }
            datePicker.maxDate = endCalendar.timeInMillis
        } else {
            datePicker.maxDate = calendar.timeInMillis
        }
    }

    val endDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            endDate = dateFormat.format(calendar.time)
            onDateRangeSelected(startDate, endDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        if (startDate.isNotEmpty()) {
            val startCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(startDate) ?: Date()
            }
            datePicker.minDate = startCalendar.timeInMillis
        } else {
            datePicker.minDate = calendar.timeInMillis
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = context.getString(R.string.date_range_picker_title),
            style = TextStyle(
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 10.dp, bottom = 14.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { startDatePickerDialog.show() },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = startDate.ifEmpty { context.getString(R.string.start_date) },
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = { endDatePickerDialog.show() },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = endDate.ifEmpty { context.getString(R.string.end_date) },
                    fontSize = 16.sp
                )
            }
        }
    }
}
