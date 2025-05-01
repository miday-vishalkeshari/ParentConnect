package com.example.parent_connect.teachers_and_students.teacherschedule

data class TeacherScheduleData(
    val classId: String = "",  // Changed from className to classId
    val subject: String = "",
    val schedulePeriod: String = ""
)
