package com.example.parent_connect.timetable

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R

class TimetableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        // Spinner setup
        val childrenDropdown: Spinner = findViewById(R.id.childrenDropdown)
        val childrenNames = listOf("Select Child", "John Doe - School A", "Jane Smith - School B", "Mark Taylor - School C")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, childrenNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        childrenDropdown.adapter = adapter

        // GridLayout setup (existing code)
        val gridTimetable: GridLayout = findViewById(R.id.gridTimetable)

        val timeSlots = listOf(
            "9:00 - 10:00",
            "10:00 - 11:00",
            "11:00 - 12:00",
            "12:00 - 1:00",
            "1:00 - 2:00"
        )

        val subjects = listOf("Math", "Science", "English", "History", "Art")
        val daysOfWeek = listOf("Mon ", "Tue ", "Wed ", "Thu ", "Fri ")

        gridTimetable.rowCount = timeSlots.size + 1
        gridTimetable.columnCount = daysOfWeek.size + 1

        val headerTextView = TextView(this).apply {
            text = "Time/Day  "
            gravity = android.view.Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            setTextColor(resources.getColor(android.R.color.white))
        }
        gridTimetable.addView(headerTextView, GridLayout.LayoutParams().apply {
            rowSpec = GridLayout.spec(0)
            columnSpec = GridLayout.spec(0)
        })

        for ((index, day) in daysOfWeek.withIndex()) {
            val dayHeader = TextView(this).apply {
                text = day
                gravity = android.view.Gravity.CENTER
                setPadding(8, 8, 8, 8)
                setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                setTextColor(resources.getColor(android.R.color.white))
            }
            gridTimetable.addView(dayHeader, GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(0)
                columnSpec = GridLayout.spec(index + 1)
            })
        }

        for ((rowIndex, timeSlot) in timeSlots.withIndex()) {
            val timeView = TextView(this).apply {
                text = timeSlot
                gravity = android.view.Gravity.CENTER
                setPadding(8, 8, 8, 8)
                setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                setTextColor(resources.getColor(android.R.color.white))
            }
            gridTimetable.addView(timeView, GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(rowIndex + 1)
                columnSpec = GridLayout.spec(0)
                setMargins(4, 4, 4, 4)
            })

            for ((colIndex, day) in daysOfWeek.withIndex()) {
                val subjectView = TextView(this).apply {
                    text = subjects[colIndex]
                    gravity = android.view.Gravity.CENTER
                    setPadding(8, 8, 8, 8)
                    setBackgroundColor(resources.getColor(android.R.color.holo_purple))
                    setTextColor(resources.getColor(android.R.color.white))
                }
                gridTimetable.addView(subjectView, GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(rowIndex + 1)
                    columnSpec = GridLayout.spec(colIndex + 1)
                    setMargins(4, 4, 4, 4)
                })
            }
        }
    }
}
