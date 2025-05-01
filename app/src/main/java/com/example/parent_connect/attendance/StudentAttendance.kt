package com.example.parent_connect.attendance

data class StudentAttendance(
    val studentName: String,
    var isPresent: Boolean = false,
    val date: String, // New variable to store the date
    val className: String
)
