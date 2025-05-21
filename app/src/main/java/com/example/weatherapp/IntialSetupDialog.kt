package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.widget.SwitchCompat

class InitialSetupDialog(
    private val context: Context,
    private val onRequestPermission: () -> Unit,
    private val onComplete: () -> Unit
) {
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_initial_setup, null)
        val dialog = AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val gpsRadio = dialogView.findViewById<RadioButton>(R.id.radioGps)
        val mapRadio = dialogView.findViewById<RadioButton>(R.id.radioMap)
        val switchNotif = dialogView.findViewById<SwitchCompat>(R.id.switchNotifications)
        val okBtn = dialogView.findViewById<Button>(R.id.btnOk)

        okBtn.setOnClickListener {
            val isGps = gpsRadio.isChecked
            val notificationsEnabled = switchNotif.isChecked

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit()
                .putString("location_mode", if (isGps) "gps" else "map")
                .putBoolean("notifications_enabled", notificationsEnabled)
                .apply()

            dialog.dismiss()
            if (isGps) {
                onRequestPermission()
            } else {
                onComplete()
            }
        }

        dialog.show()
    }
}
