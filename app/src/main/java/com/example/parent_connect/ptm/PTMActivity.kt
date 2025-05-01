package com.example.parent_connect.ptm

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PTMActivity : AppCompatActivity() {
    private lateinit var userRole: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ptm)

        // Get the user role passed in the intent
        userRole = intent.getStringExtra("USER_ROLE") ?: ""
        Log.d("PTMActivity", "User Role: $userRole")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Example data for RecyclerView
        val homeworkList = listOf(
            Meeting("2025-01-30", "Math Homework", "10:00 AM", "Solve equations"),
            Meeting("2025-01-31", "Science Homework", "12:00 PM", "Write a report")
        )

        // Set the RecyclerView Adapter
        recyclerView.adapter = MeetingAdapter(homeworkList)

        // Initialize Spinners
        val childrenDropdown = findViewById<Spinner>(R.id.childrenDropdown)
        val classDropdown = findViewById<Spinner>(R.id.classDropdown)

        // Example data for Spinners
        val children = listOf("Child 1", "Child 2", "Child 3")
        val classes = listOf("Class A", "Class B", "Class C")

        // Set up Spinner adapters
        childrenDropdown.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, children)
        childrenDropdown.setSelection(0)  // Select the first item by default

        classDropdown.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classes)
        classDropdown.setSelection(0)  // Select the first item by default
    }
}
