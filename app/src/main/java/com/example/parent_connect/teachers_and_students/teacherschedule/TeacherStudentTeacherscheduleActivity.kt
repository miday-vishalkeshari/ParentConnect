package com.example.parent_connect.teachers_and_students.teacherschedule

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class TeacherStudentTeacherscheduleActivity : AppCompatActivity() {

    private lateinit var teacherRecyclerView: RecyclerView
    private lateinit var teacherAdapter: TeacherAdapter
    private lateinit var personAdapter: PersonAdapter
    private lateinit var loadingSpinner: ProgressBar // ProgressBar to show loading state
    private var isLoading = true // Variable to track loading state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_teacherschedule_student_teacher)

        teacherRecyclerView = findViewById(R.id.teacherRecyclerView)
        teacherRecyclerView.layoutManager = LinearLayoutManager(this)

        // Reference to the ProgressBar
        loadingSpinner = findViewById(R.id.loadingSpinner)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val role = intent.getStringExtra("USER_ROLE") ?: "DefaultRole"
        Log.d("TeacherScheduleActivity", "User Role = $role")

        val teacherScheduleList  = listOf(
            TeacherScheduleData("6A", "Mathematics", "Period: 1 [9:00 to 9:45]"),
            TeacherScheduleData("6A", "Science", "Period: 2 [9:45 to 10:30]"),
            TeacherScheduleData("6A", "English", "Period: 3 [10:30 to 11:15]")
        )

        teacherAdapter = TeacherAdapter(teacherScheduleList)
        teacherRecyclerView.adapter = teacherAdapter

        val isTeacher = intent.getBooleanExtra("isTeacher", true)
        val classId = intent.getStringExtra("CLASS_ID")
        val section = intent.getStringExtra("SECTION")

        Log.d("TeacherScheduleActivity", "classId: $classId")
        Log.d("TeacherScheduleActivity", "section: $section")

        val fullClassId = "$classId$section"
        Log.d("TeacherScheduleActivity", "fullClassId: $fullClassId")

        // Reference to the TextView
        val teachersStudentsTextView: TextView = findViewById(R.id.teachersStudentsTextView)

        if (isTeacher) {
            Log.d("TeacherScheduleActivity", "Teacher mode enabled")
            // Set the text for the Teacher mode
            teachersStudentsTextView.text = "List of Teachers in the class $fullClassId"

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"
            // Show the loader while fetching data
            showLoadingState()
            fetchTeacherListFromFirestore(userId, classId, section) { teacherList ->
                // Hide loader after data is fetched
                hideLoadingState()
                personAdapter = PersonAdapter(teacherList)
                teacherRecyclerView.adapter = personAdapter
                if (teacherList.isEmpty()) {
                    showEmptyState()
                }
            }
        } else {
            Log.d("TeacherScheduleActivity", "Student mode enabled")
            // Set the text for the Student mode
            teachersStudentsTextView.text = "List of Students in the class $fullClassId"

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"
            // Show the loader while fetching data
            showLoadingState()
            Log.d("TeacherScheduleActivity", "vk test3-- schoolId fetched: $fullClassId")
            fetchStudentListFromFirestore(userId, fullClassId) { studentList ->
                // Hide loader after data is fetched
                hideLoadingState()
                personAdapter = PersonAdapter(studentList)
                teacherRecyclerView.adapter = personAdapter
                if (studentList.isEmpty()) {
                    showEmptyState()
                }
            }
        }
    }

    private fun showEmptyState() {
        // Handle the empty state, such as showing a message or a placeholder
        Log.d("TeacherScheduleActivity", "No data found.")

        val emptyClassTextView: TextView = findViewById(R.id.empty_class)
        emptyClassTextView.visibility = View.VISIBLE

        // Hide the RecyclerView
        teacherRecyclerView.visibility = View.GONE
    }

    private fun showLoadingState() {
        // Show the loader when fetching data
        loadingSpinner.visibility = View.VISIBLE
        teacherRecyclerView.visibility = View.GONE // Hide RecyclerView while loading
    }

    private fun hideLoadingState() {
        // Hide the empty class TextView
        val emptyClassTextView: TextView = findViewById(R.id.empty_class)
        emptyClassTextView.visibility = View.GONE

        // Hide the loader once data is loaded
        loadingSpinner.visibility = View.GONE
        teacherRecyclerView.visibility = View.VISIBLE // Show RecyclerView after loading
    }

    // Fetch teacher data
    private fun fetchTeacherListFromFirestore(userId: String, classId: String?, section: String?, callback: (List<PersonData>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { userDoc ->
            val schoolId = userDoc.getString("schoolId") ?: "defaultSchoolId"
            Log.d("TeacherScheduleActivity", "schoolId fetched: $schoolId")

            val teacherRef = db.collection("Schools").document(schoolId)
                .collection("Classes").document("$classId$section")
                .collection("teachers")

            teacherRef.get().addOnSuccessListener { result ->
                val teacherList = mutableListOf<PersonData>()
                for (document in result) {
                    val teacherName = document.getString("name") ?: "Unknown"
                    val teacherId = document.getString("teacherId") ?: "Unknown"
                    teacherList.add(PersonData(teacherName, teacherId))
                }
                callback(teacherList)
            }.addOnFailureListener { exception ->
                Log.w("TeacherScheduleActivity", "Error getting teacher documents: ", exception)
                callback(emptyList()) // Return empty list in case of error
            }
        }.addOnFailureListener { exception ->
            Log.w("TeacherScheduleActivity", "Error getting user document: ", exception)
            callback(emptyList())
        }
    }

    // Fetch student data
    private fun fetchStudentListFromFirestore(userId: String, fullClassId: String?, callback: (List<PersonData>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { userDoc ->
            val schoolId = userDoc.getString("schoolId") ?: "defaultSchoolId"
            Log.d("TeacherScheduleActivity", "schoolId fetched: $schoolId")

            val studentRef = db.collection("Schools").document(schoolId)
                .collection("Classes").document("$fullClassId")
                .collection("students")

            Log.d("TeacherScheduleActivity", "vk test2-- schoolId fetched: $fullClassId")

            studentRef.get().addOnSuccessListener { result ->
                val studentList = mutableListOf<PersonData>()
                for (document in result) {
                    val studentName = document.getString("name") ?: "Unknown"
                    val rollNo = document.getString("rollno") ?: "Unknown"
                    val studentId = document.getString("studentId") ?: "Unknown"
                    studentList.add(PersonData(studentName, studentId))

                    Log.d("TeacherScheduleActivity", "vk test1-- schoolId fetched: $schoolId")
                }
                callback(studentList)
            }.addOnFailureListener { exception ->
                Log.w("TeacherScheduleActivity", "Error getting student documents: ", exception)
                callback(emptyList()) // Return empty list in case of error
            }
        }.addOnFailureListener { exception ->
            Log.w("TeacherScheduleActivity", "Error getting user document: ", exception)
            callback(emptyList())
        }
    }
}
