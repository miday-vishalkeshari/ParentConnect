package com.example.parent_connect.insideclass

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import com.example.parent_connect.attendance.AttendanceActivity // Import the AttendanceActivity
import com.example.parent_connect.teachers_and_students.teacherschedule.TeacherStudentTeacherscheduleActivity

class InsideClassActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inside_class)

        // Get classId and section from the Intent
        val classId = intent.getStringExtra("CLASS_ID")
        val section = intent.getStringExtra("SECTION")

        // Reference to the TextView that will show the selected class and section
        val classSectionTextView: TextView = findViewById(R.id.classSectionTextView)

        // Check if classId and section are not null, then display them in the TextView
        if (classId != null && section != null) {
            classSectionTextView.text = "Class: $classId, Section: $section"
            classSectionTextView.visibility = android.view.View.VISIBLE // Make it visible
        }

        // Reference to the attendance TextView
        val attendanceTextView: TextView = findViewById(R.id.attendanceTextView)

        // Set an OnClickListener for the attendanceTextView
        attendanceTextView.setOnClickListener {
            // Create an Intent to open the AttendanceActivity
            val intent = Intent(this, AttendanceActivity::class.java)

            // You can pass extra data if needed (e.g., classId, section, etc.)
            intent.putExtra("CLASS_ID", classId)
            intent.putExtra("SECTION", section)

            // Start the AttendanceActivity
            startActivity(intent)
        }

        // Reference to the attendance TextView
        val teacherTextView: TextView = findViewById(R.id.teachersTextView)

        teacherTextView.setOnClickListener {
            // Create an Intent to open the TeacherStudentTeacherscheduleActivity
            val intent = Intent(this, TeacherStudentTeacherscheduleActivity::class.java)

            // Pass extra data such as classId and section
            intent.putExtra("CLASS_ID", classId)  // Replace classId with the actual variable holding the class ID
            intent.putExtra("SECTION", section)  // Replace section with the actual variable holding the section

            // Pass additional data if needed (e.g., isTeacher flag)
            intent.putExtra("isTeacher", true)

            // Start the TeacherStudentTeacherscheduleActivity
            startActivity(intent)
        }


        // Reference to the attendance TextView
        val studentsTextView: TextView = findViewById(R.id.studentsTextView)

        // Set an OnClickListener for the attendanceTextView
        studentsTextView.setOnClickListener {
            // Create an Intent to open the AttendanceActivity
            val intent = Intent(this, TeacherStudentTeacherscheduleActivity::class.java)

            // Pass extra data such as classId and section
            intent.putExtra("CLASS_ID", classId)  // Replace classId with the actual variable holding the class ID
            intent.putExtra("SECTION", section)  // Replace section with the actual variable holding the section

            // You can pass extra data if needed (e.g., classId, section, etc.)
            intent.putExtra("isTeacher", false)

            // Start the AttendanceActivity
            startActivity(intent)
        }
    }
}
