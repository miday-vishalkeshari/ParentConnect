package com.example.parent_connect.settings

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var notificationSwitch: Switch
    private lateinit var noticeboardSwitch: Switch
    private lateinit var messageSwitch: Switch
    private lateinit var attendanceSwitch: Switch
    private lateinit var ptmSwitch: Switch
    private lateinit var feeReminderSwitch: Switch
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI components
        notificationSwitch = findViewById(R.id.notificationSwitch)
        noticeboardSwitch = findViewById(R.id.noticeboardSwitch)
        messageSwitch = findViewById(R.id.messageSwitch)
        attendanceSwitch = findViewById(R.id.attendanceSwitch)
        ptmSwitch = findViewById(R.id.ptmSwitch)
        feeReminderSwitch = findViewById(R.id.feeReminderSwitch)
        saveButton = findViewById(R.id.saveButton)

        // Add listeners to each switch to show a Toast when the state changes
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        noticeboardSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Noticeboard notifications enabled" else "Noticeboard notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        messageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Message notifications enabled" else "Message notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        attendanceSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Attendance notifications enabled" else "Attendance notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        ptmSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "PTM notifications enabled" else "PTM notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        feeReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Fee reminder notifications enabled" else "Fee reminder notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Save button functionality
        saveButton.setOnClickListener {
            val allNotifications = """
                General Notifications: ${notificationSwitch.isChecked}
                Noticeboard Notifications: ${noticeboardSwitch.isChecked}
                Message Notifications: ${messageSwitch.isChecked}
                Attendance Notifications: ${attendanceSwitch.isChecked}
                PTM Notifications: ${ptmSwitch.isChecked}
                Fee Reminder Notifications: ${feeReminderSwitch.isChecked}
            """.trimIndent()

            Toast.makeText(this, "Settings Saved: $allNotifications", Toast.LENGTH_LONG).show()
        }
    }
}
