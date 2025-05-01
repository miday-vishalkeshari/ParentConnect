package com.example.parent_connect.attendance

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parent_connect.R
import com.example.parent_connect.databinding.ActivityAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var role: String
    private var selectedClass: String? = null
    private var selectedMonth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user role from the intent
        role = intent.getStringExtra("USER_ROLE") ?: ""


        // Initialize the RecyclerView adapter
        attendanceAdapter = AttendanceAdapter(role)
        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.studentsRecyclerView.adapter = attendanceAdapter

        // Setup child spinner for parent role
        if (role == "Parent") {
            Log.d("AttendanceActivity", "we are here in parents role")
            binding.cardViewClassSelector.visibility = View.GONE
            binding.cardViewDateSelector.visibility = View.GONE

            setupChildSpinner()
        }
        else {
            binding.cardViewChildSelector.visibility = View.GONE
            // Setup  spinner
            fetchSchoolIdForSchoolAndsetupMonthSpinner()
            setupSpinners()
        }
    }

    private fun setupSpinners() {
        // Class spinner
        binding.classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedClassName = parent.getItemAtPosition(position).toString()
                if (selectedClassName != selectedClass) {
                    selectedClass = selectedClassName
                    resetSpinners()  // Reset the month and date when class is changed
                    //setupMonthSpinner(selectedClassName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        // Month spinner
        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = parent.getItemAtPosition(position).toString()
                setupDateSpinner(selectedMonth!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun resetSpinners() {
        binding.monthSpinner.setSelection(0) // Reset month spinner
        binding.dateSpinner.setSelection(0) // Reset date spinner
        selectedMonth = null
    }

    // Function to get the current logged-in user and handle errors
    private fun getCurrentUserId(): String? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("AttendanceActivity", "No user is logged in.")
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
        return userId
    }

    private fun setupChildSpinner() {
        val db = FirebaseFirestore.getInstance()
        val childList = mutableListOf<String>()
        val childDetailsMap =
            mutableMapOf<String, String>() // Map to store admissionNo -> schoolId mapping

        // Get the current user ID
        val userId = getCurrentUserId() ?: return

        Log.d("AttendanceActivity", "Starting to fetch children for userId: $userId")

        // Get the list of wards (children) for the given userId
        db.collection("users")
            .document(userId)
            .collection("Wards")
            .get()
            .addOnSuccessListener { result ->
                Log.d("AttendanceActivity", "Successfully fetched wards data")

                // Iterate through the result and extract admissionNo and schoolId
                for (document in result) {
                    val admissionNo = document.getString("admissionNo")
                    val schoolId = document.getString("schoolId")

                    if (admissionNo != null && schoolId != null) {
                        Log.d(
                            "AttendanceActivity",
                            "admission number child: $admissionNo with schoolId: $schoolId"
                        )
                        childList.add(admissionNo)
                        childDetailsMap[admissionNo] = schoolId // Map admissionNo to schoolId
                    } else {
                        Log.w(
                            "AttendanceActivity",
                            "Ward document does not contain required fields"
                        )
                    }
                }

                if (childList.isEmpty()) {
                    Log.w("AttendanceActivity", "No children found for this user.")
                } else {
                    Log.d("AttendanceActivity", "Children fetched: $childList")
                }

                // Set up the spinner with the fetched child list
                val childAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, childList)
                childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.childSpinner.adapter = childAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("AttendanceActivity", "Error getting children list: $exception")
                Toast.makeText(this, "Error getting children list: $exception", Toast.LENGTH_SHORT)
                    .show()
            }

        binding.childSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedAdmissionNo = parent.getItemAtPosition(position).toString()
                Log.d("AttendanceActivity", "Child selected: $selectedAdmissionNo")

                // Retrieve the corresponding schoolId from the map
                val selectedSchoolId = childDetailsMap[selectedAdmissionNo]

                if (selectedSchoolId != null) {
                    Log.d(
                        "AttendanceActivity",
                        "Selected Child Details - Admission No: $selectedAdmissionNo, School ID: $selectedSchoolId"
                    )

                    // Call the setupMonthSelector with the schoolId and admissionNo
                    setupMonthSelector(selectedSchoolId, selectedAdmissionNo)

                    Toast.makeText(
                        this@AttendanceActivity,
                        "Selected Child: $selectedAdmissionNo",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(
                        "AttendanceActivity",
                        "No schoolId found for selected child: $selectedAdmissionNo"
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("AttendanceActivity", "Nothing selected in child spinner")
            }
        }
    }

    private fun setupMonthSelector(schoolId: String, admissionNo: String) {
        val db = FirebaseFirestore.getInstance()
        val studentAttendanceList = mutableListOf<StudentAttendance>()  // List to hold attendance data for students in the selected month
        val monthList = mutableListOf<String>()  // List to hold months for the student

        // Create the adapter for the month spinner
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthList)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.monthSpinner.adapter = monthAdapter

        // Clear the attendance list initially before fetching data
        studentAttendanceList.clear()  // Clear previous attendance data
        attendanceAdapter.updateList(studentAttendanceList)  // Update the RecyclerView with an empty list

        Log.d("AttendanceActivity", "Starting to fetch months for student: $admissionNo")

        // Get the months for the student
        db.collection("Schools")
            .document(schoolId)
            .collection("Students")
            .document(admissionNo)
            .collection("months")
            .get()
            .addOnSuccessListener { result ->
                Log.d("AttendanceActivity", "Successfully fetched months data for student: $admissionNo")

                if (!result.isEmpty) {
                    monthList.clear()  // Clear the existing month list

                    // Loop through all month documents
                    result.documents.forEach { monthDoc ->
                        val monthName = monthDoc.id  // This will be the month name like "February", "March", etc.
                        Log.d("AttendanceActivity", "Found month: $monthName")

                        monthList.add(monthName)  // Add the month to the list (for showing in the spinner)

                        // Fetch the attendance data for each day in this month document
                        monthDoc.data?.forEach { (day, attendance) ->
                            // Check if the field is a valid day and if the attendance is a boolean value
                            if (day.toIntOrNull() != null && attendance is Boolean) {
                                Log.d("AttendanceActivity", "Month: $monthName, Day: $day, Attendance: $attendance")

                                // Create a StudentAttendance object for each day's attendance data
                                val studentAttendance = StudentAttendance(
                                    studentName = admissionNo,  // Use the admission number as the student's name
                                    isPresent = attendance,  // Attendance status for that day
                                    date = day,  // The day (field name like "1", "2", etc.)
                                    className = "6A"  // Class name, assuming "6A"
                                )
                                studentAttendanceList.add(studentAttendance)
                            }
                        }
                    }

                    // Log and update the UI with the full attendance list
                    Log.d("AttendanceActivity", "Final attendance list: $studentAttendanceList")

                    // Now update the spinner with the months
                    monthAdapter.notifyDataSetChanged()  // Notify the spinner adapter that the data has changed
                } else {
                    Log.d("AttendanceActivity", "No months found for student: $admissionNo")
                    Toast.makeText(this, "No months available for this student", Toast.LENGTH_SHORT).show()

                    // Clear the month list and update the spinner
                    monthList.clear()
                    monthAdapter.notifyDataSetChanged()  // Notify the spinner adapter that the data has changed
                }
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceActivity", "Error fetching months for student: $admissionNo, error: ${e.localizedMessage}")
                Toast.makeText(this, "Error fetching months: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()

                // Clear month list and update the spinner in case of failure
                monthList.clear()
                monthAdapter.notifyDataSetChanged()  // Notify the spinner adapter that the data has changed
            }

        // Handle month selection
        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMonth = parent.getItemAtPosition(position)?.toString() ?: return
                Log.d("AttendanceActivity", "Month selected: $selectedMonth")

                // Fetch and display attendance data for the selected month
                setupDateSpinnerForMonthForParentview(schoolId, admissionNo, selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("AttendanceActivity", "No month selected")
            }
        }
    }

    //vk1
    private fun setupDateSpinnerForMonthForParentview(schoolId: String, studentId: String, month: String) {
        val db = FirebaseFirestore.getInstance()
        val dateList = mutableListOf<String>()
        val studentAttendanceList = mutableListOf<StudentAttendance>()

        Log.d("AttendanceActivity", "Fetching attendance for School: $schoolId, Student: $studentId, Month: $month")

        db.collection("Schools")
            .document(schoolId)
            .collection("Students")
            .document(studentId)
            .collection("months")
            .document(month)
            .get()
            .addOnSuccessListener { monthDoc ->
                if (monthDoc.exists()) {
                    Log.d("AttendanceActivity", "Month data found for $month")

                    // Extract attendance data
                    monthDoc.data?.forEach { (date, isPresent) ->
                        if (isPresent is Boolean) {
                            Log.d("AttendanceActivity", "Date: $date, Attendance: $isPresent")

                            val studentAttendance = StudentAttendance(
                                studentName = studentId, // Assuming studentId is used as name
                                isPresent = isPresent,
                                date = date,
                                className = "6A" // Replace with actual class name if available
                            )
                            studentAttendanceList.add(studentAttendance)
                            dateList.add(date) // Add date for spinner
                        }
                    }

                    Log.d("AttendanceActivity", "Final attendance list: $studentAttendanceList")

                    // Update RecyclerView adapter with the full list
                    attendanceAdapter.updateList(studentAttendanceList)

                    // Populate date spinner with distinct dates
                    val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateList.distinct())
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dateSpinner.adapter = dateAdapter

                    // Handle date selection
                    binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedDate = parent.getItemAtPosition(position).toString()
                            Log.d("AttendanceActivity", "Date selected: $selectedDate")

                            // Filter attendance list by selected date
                            val filteredList = studentAttendanceList.filter { it.date == selectedDate }
                            attendanceAdapter.updateList(filteredList)
                            Toast.makeText(this@AttendanceActivity, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            Log.d("AttendanceActivity", "No date selected")
                        }
                    }
                } else {
                    Log.d("AttendanceActivity", "No attendance data found for $month")
                    Toast.makeText(this, "No attendance data available for $month", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceActivity", "Error fetching attendance: ${e.localizedMessage}")
                Toast.makeText(this, "Error fetching attendance: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchSchoolIdForSchoolAndsetupMonthSpinner() {
        Log.d("AttendanceActivity", "Fetching school ID and notices from Firestore...")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get the logged-in user's ID
            Log.d("AttendanceActivity", "User ID: $userId")

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)

            // Fetch the schoolId directly from the user's document
            userDocRef.get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val schoolId = userDocument.getString("schoolId") // Fetch schoolId
                        if (schoolId != null) {
                            Log.d("AttendanceActivity", "School ID(fetchSchoolIdForSchoolAndsetupMonthSpinner): $schoolId")
                            setupClassSpinner(schoolId)
                            //setupMonthSpinner(schoolId) // Fetch notices for this schoolId
                        } else {
                            Log.e("AttendanceActivity", "No schoolId found for user(fetchSchoolIdForSchoolAndsetupMonthSpinner)")
                            Toast.makeText(this, "School ID not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("AttendanceActivity", "User document not found(fetchSchoolIdForSchoolAndsetupMonthSpinner)")
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AttendanceActivity", "Failed to fetch user: ${exception.message}(fetchSchoolIdForSchoolAndsetupMonthSpinner)")
                    Toast.makeText(this, "Failed to fetch user", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("AttendanceActivity", "No user logged in(fetchSchoolIdForSchoolAndsetupMonthSpinner)")
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateSpinner(month: String) {
        val db = FirebaseFirestore.getInstance()
        val dateList = mutableListOf<String>()
        val studentAttendanceList = mutableListOf<StudentAttendance>()
        val studentList = mutableListOf<String>()

        Log.d("AttendanceActivity", "Starting Firestore query to get dates for month: $month")

        db.collection("Schools")
            .document("sch1")
            .collection("Classes")
            .document("6A")
            .collection("attendance")
            .document(month)
            .collection("Students")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("AttendanceActivity", "Firestore query successful")

                if (!querySnapshot.isEmpty) {
                    // Loop through all students for the selected month
                    querySnapshot.documents.forEach { studentDoc ->
                        val studentName = studentDoc.id
                        studentList.add(studentName)

                        // Fetch attendance for each student
                        studentDoc.data?.forEach { (date, isPresent) ->
                            if (isPresent is Boolean) {
                                Log.d("AttendanceActivity", "Student: $studentName, Date: $date, Attendance: $isPresent")
                                // Add the attendance record for each student for that date
                                val studentAttendance = StudentAttendance(
                                    studentName = studentName,
                                    isPresent = isPresent,
                                    date = date,
                                    className = "6A"
                                )
                                studentAttendanceList.add(studentAttendance)
                                dateList.add(date) // Add the date to the list
                            }
                        }
                    }

                    Log.d("AttendanceActivity", "Final attendance list: $studentAttendanceList")

                    // Update the UI or Adapter with the studentAttendanceList
                    attendanceAdapter.updateList(studentAttendanceList)

                    // Populate the spinner with the dates
                    val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateList.distinct())
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dateSpinner.adapter = dateAdapter

                    // Handle date selection
                    binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedDate = parent.getItemAtPosition(position).toString()
                            Log.d("AttendanceActivity", "Date selected: $selectedDate")
                            Toast.makeText(this@AttendanceActivity, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()

                            // Filter the attendance list by the selected date
                            val filteredList = studentAttendanceList.filter { it.date == selectedDate }
                            attendanceAdapter.updateList(filteredList)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            Log.d("AttendanceActivity", "No date selected")
                        }
                    }
                } else {
                    Log.d("AttendanceActivity", "No students found for the selected month")
                    Toast.makeText(this, "No students available for this month", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceActivity", "Error fetching students: ${e.localizedMessage}")
                Toast.makeText(this, "Error fetching students: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDateSpinnerForClassAndMonth(schoolId: String,classId: String,month: String) {
        val db = FirebaseFirestore.getInstance()
        val dateList = mutableListOf<String>()
        val studentAttendanceList = mutableListOf<StudentAttendance>()
        val studentList = mutableListOf<String>()

        Log.d("AttendanceActivity", "Starting Firestore query to get dates for month: $month")

        db.collection("Schools")
            .document(schoolId)
            .collection("Classes")
            .document(classId)
            .collection("attendance")
            .document(month)
            .collection("Students")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("AttendanceActivity", "Firestore query successful")

                if (!querySnapshot.isEmpty) {
                    // Loop through all students for the selected month
                    querySnapshot.documents.forEach { studentDoc ->
                        val studentName = studentDoc.id
                        studentList.add(studentName)

                        // Fetch attendance for each student
                        studentDoc.data?.forEach { (date, isPresent) ->
                            if (isPresent is Boolean) {
                                Log.d("AttendanceActivity", "Student: $studentName, Date: $date, Attendance: $isPresent")
                                // Add the attendance record for each student for that date
                                val studentAttendance = StudentAttendance(
                                    studentName = studentName,
                                    isPresent = isPresent,
                                    date = date,
                                    className = "6A"
                                )
                                studentAttendanceList.add(studentAttendance)
                                dateList.add(date) // Add the date to the list
                            }
                        }
                    }

                    Log.d("AttendanceActivity", "Final attendance list: $studentAttendanceList")

                    // Update the UI or Adapter with the studentAttendanceList
                    attendanceAdapter.updateList(studentAttendanceList)

                    // Populate the spinner with the dates
                    val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateList.distinct())
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dateSpinner.adapter = dateAdapter

                    // Handle date selection
                    binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedDate = parent.getItemAtPosition(position).toString()
                            Log.d("AttendanceActivity", "Date selected: $selectedDate")
                            Toast.makeText(this@AttendanceActivity, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()

                            // Filter the attendance list by the selected date
                            val filteredList = studentAttendanceList.filter { it.date == selectedDate }
                            attendanceAdapter.updateList(filteredList)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            Log.d("AttendanceActivity", "No date selected")
                        }
                    }
                } else {
                    Log.d("AttendanceActivity", "No students found for the selected month")
                    Toast.makeText(this, "No students available for this month", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceActivity", "Error fetching students: ${e.localizedMessage}")
                Toast.makeText(this, "Error fetching students: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupMonthSpinnerForClass(schoolId: String, classId: String) {
        val db = FirebaseFirestore.getInstance()
        val monthList = mutableListOf<String>()

        Log.d("AttendanceActivity", "Starting Firestore query to get months for class: $classId")

        // Query Firestore to get the documents representing the months for the specific class
        db.collection("Schools")
            .document(schoolId)
            .collection("Classes")
            .document(classId)
            .collection("attendance") // Collection of months (January, February, etc.)
            .get()
            .addOnSuccessListener { monthQuerySnapshot ->
                Log.d("AttendanceActivity", "Firestore query successful for class: $classId")

                if (!monthQuerySnapshot.isEmpty) {
                    // Loop through each month document and add it to the month list
                    monthQuerySnapshot.documents.forEach { document ->
                        val month = document.id // The document ID will be the month name
                        if (!monthList.contains(month)) {
                            monthList.add(month) // Add the month to the list (avoiding duplicates)
                        }
                    }

                    Log.d("AttendanceActivity", "Found months: $monthList")

                    // Populate the spinner with the distinct month list
                    val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthList)
                    monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.monthSpinner.adapter = monthAdapter

                    // Handle month selection
                    binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedMonth = parent.getItemAtPosition(position).toString()
                            Log.d("AttendanceActivity", "Month selected: $selectedMonth")
                            Toast.makeText(this@AttendanceActivity, "Selected Month: $selectedMonth", Toast.LENGTH_SHORT).show()

                            // Load the dates for the selected month
                            //setupDateSpinner(selectedMonth)
                            setupDateSpinnerForClassAndMonth(schoolId,classId,selectedMonth)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            Log.d("AttendanceActivity", "No month selected")
                        }
                    }
                } else {
                    Log.d("AttendanceActivity", "No months found for class $classId")
                    Toast.makeText(this, "No months found for the selected class", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceActivity", "Error fetching months for class $classId: ${e.localizedMessage}")
                Toast.makeText(this, "Error fetching months: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClassSpinner(schoolId: String) {
        val db = FirebaseFirestore.getInstance()
        val classList = mutableListOf<String>()

        Log.d("AttendanceActivity", "Starting to fetch class list from Firestore")

        // Get the list of classes from Firestore
        db.collection("Schools")
            .document(schoolId)
            .collection("Classes")
            .get()
            .addOnSuccessListener { result ->
                Log.d("AttendanceActivity", "Successfully fetched class data")

                // Iterate through the result and directly use document ID as class name
                for (document in result) {
                    val classId = document.id // Use the document ID as class name
                    Log.d("AttendanceActivity", "Found class: $classId")
                    classList.add(classId)
                }

                if (classList.isEmpty()) {
                    Log.w("AttendanceActivity", "No classes found in Firestore.")
                } else {
                    Log.d("AttendanceActivity", "Classes fetched: $classList")
                }

                // Set up the spinner with the fetched class list
                val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classList)
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.classSpinner.adapter = classAdapter

            }
            .addOnFailureListener { exception ->
                Log.e("AttendanceActivity", "Error getting class list: $exception")
                Toast.makeText(this, "Error getting class list: $exception", Toast.LENGTH_SHORT).show()
            }

        // Handle class selection
        binding.classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedClass = parent.getItemAtPosition(position).toString()
                Log.d("AttendanceActivity", "Class selected: $selectedClass")

                // Call setupMonthSpinnerForClass with the selected class ID
                setupMonthSpinnerForClass(schoolId, selectedClass)

                Toast.makeText(this@AttendanceActivity, "Selected Class: $selectedClass", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("AttendanceActivity", "Nothing selected in class spinner")
            }
        }
    }

}

