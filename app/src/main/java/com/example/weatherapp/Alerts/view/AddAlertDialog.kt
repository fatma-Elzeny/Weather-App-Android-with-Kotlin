package com.example.weatherapp.Alerts.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import com.example.weatherapp.R
import com.example.weatherapp.data.model.WeatherAlert
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddAlertDialog(
    private val context: Context,
    private val onAlertCreated: (WeatherAlert) -> Unit
) {

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_alert, null)
        val fromBtn = view.findViewById<Button>(R.id.btnFrom)
        val toBtn = view.findViewById<Button>(R.id.btnTo)
        val alarmRadio = view.findViewById<RadioButton>(R.id.radioAlarm)
        val notificationRadio = view.findViewById<RadioButton>(R.id.radioNotification)
        val saveBtn = view.findViewById<Button>(R.id.btnSave)

        var fromTime = 0L
        var toTime = 0L

        fun pickDateTime(onPicked: (Long) -> Unit) {
            val now = Calendar.getInstance()
            DatePickerDialog(context, { _, y, m, d ->
                TimePickerDialog(context, { _, h, min ->
                    val cal = Calendar.getInstance().apply {
                        set(y, m, d, h, min)
                    }
                    onPicked(cal.timeInMillis)
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        fromBtn.setOnClickListener {
            pickDateTime {
                fromTime = it
                fromBtn.text = SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(Date(it))
            }
        }

        toBtn.setOnClickListener {
            pickDateTime {
                toTime = it
                toBtn.text = SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(
                    Date(it)
                )
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        saveBtn.setOnClickListener {
            if (fromTime == 0L || toTime == 0L || toTime <= fromTime) {
                Toast.makeText(context, "Please select valid times", Toast.LENGTH_SHORT).show()
            } else {
                val alert = WeatherAlert(
                    fromTime = fromTime,
                    toTime = toTime,
                    isActive = alarmRadio.isChecked
                )
                onAlertCreated(alert)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
