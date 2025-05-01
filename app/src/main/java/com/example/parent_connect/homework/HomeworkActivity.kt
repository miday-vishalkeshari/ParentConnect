package com.example.parent_connect.homework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeworkActivity : AppCompatActivity() {

    private lateinit var homeworkAdapter: HomeworkAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var childrenSpinner: Spinner
    private lateinit var classSpinner: Spinner
    private lateinit var addHomeworkButton: Button
    private var selectedWard: String? = null
    private var selectedClass: String? = null
    private var role: String? = null

    // Register the ActivityResultLauncher
    private val addHomeworkLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Homework added successfully, refresh the list
                refreshHomeworkList(selectedClass ?: "", "sch1")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homework)

        // Initialize UI components
        childrenSpinner = findViewById(R.id.childrenDropdown)
        classSpinner = findViewById(R.id.classDropdown)
        addHomeworkButton = findViewById(R.id.addHomeworkButton)
        recyclerView = findViewById(R.id.recyclerViewHomework)

        // Get the user role from the intent
        role = intent.getStringExtra("USER_ROLE") ?: ""

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data and setup spinners
        fetchWardsFromBackend { wardList ->
            setupChildrenSpinner(wardList)
        }

        val schoolId = "sch1" // Replace with actual school ID
        setupClassSpinner(schoolId)

        // Show or hide spinners based on role
        if (role == "Parent") {
            childrenSpinner.visibility = View.VISIBLE
            classSpinner.visibility = View.GONE
            addHomeworkButton.visibility = View.GONE // Hide the button for parents
        } else {
            childrenSpinner.visibility = View.GONE
            classSpinner.visibility = View.VISIBLE
            addHomeworkButton.visibility = View.VISIBLE // Show the button for other roles
        }

        // Setup add homework button
        addHomeworkButton.setOnClickListener {
            if (selectedClass.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AddHomeworkActivity::class.java)
                intent.putExtra("SCHOOL_ID", schoolId)  // Pass schoolId
                intent.putExtra("CLASS_NAME", selectedClass)  // Pass selected class ID
                addHomeworkLauncher.launch(intent) // Launch AddHomeworkActivity with result handling
            }
        }

    }

    private fun refreshHomeworkList(selectedClass: String, schoolId: String) {
        // This function reloads the homework list
        loadHomeworkforwholeclass(schoolId, selectedClass)
    }

    private fun fetchWardsFromBackend(callback: (List<String>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("fetchWardsFromBackend", "User not logged in")
            callback(listOf("Select Child"))
            return
        }

        val uid = user.uid
        val db = FirebaseFirestore.getInstance()
        val wardsList = mutableListOf("Select Child") // Default option

        db.collection("users")
            .document(uid)
            .collection("Wards")
            .get()
            .addOnSuccessListener { result ->
                // Loop through each ward (child)
                for (document in result) {
                    val schoolId = document.getString("schoolId") ?: "Unknown School"
                    val admissionNo = document.getString("admissionNo") ?: "Unknown Admission No"

                    // Fetch class and section for each student
                    db.collection("Schools")
                        .document(schoolId)
                        .collection("Students")
                        .document(admissionNo)
                        .get()
                        .addOnSuccessListener { studentDoc ->
                            val className = studentDoc.getString("class") ?: "Unknown Class"
                            val section = studentDoc.getString("section") ?: ""

                            // Add the class and section to the list in the format you prefer
                            wardsList.add("Admission No: $admissionNo - School: $schoolId - Class: $className$section")

                            // Only call the callback when all documents have been processed
                            if (result.size() == wardsList.size - 1) {
                                callback(wardsList)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("fetchWardsFromBackend", "Error fetching class and section: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchWardsFromBackend", "Error fetching wards: $exception")
                callback(listOf("Select Child")) // Show only default option in case of failure
            }
    }



    private fun setupClassSpinner(schoolId: String) {
        val db = FirebaseFirestore.getInstance()
        val classList = mutableListOf("Select Class") // Add "Select Class" option initially

        Log.d("HomeworkActivity", "Starting to fetch class list from Firestore")

        // Get the list of classes from Firestore
        db.collection("Schools")
            .document(schoolId)
            .collection("Classes")
            .get()
            .addOnSuccessListener { result ->
                Log.d("HomeworkActivity", "Successfully fetched class data")

                // Iterate through the result and directly use document ID as class name
                for (document in result) {
                    val classId = document.id // Use the document ID as class name
                    Log.d("HomeworkActivity", "Found class: $classId")
                    classList.add(classId)
                }

                if (classList.isEmpty()) {
                    Log.w("HomeworkActivity", "No classes found in Firestore.")
                } else {
                    Log.d("HomeworkActivity", "Classes fetched: $classList")
                }

                // Set up the spinner with the fetched class list
                val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classList)
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                classSpinner.adapter = classAdapter

                classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedClass = if (position > 0) classList[position] else null
                        // Pass both schoolId and selectedClass to loadHomeworkforwholeclass
                        if (selectedClass != null) {
                            loadHomeworkforwholeclass(schoolId, selectedClass!!)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedClass = null
                        // Clear the homework if no class is selected
                        recyclerView.adapter = null
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeworkActivity", "Error getting class list: $exception")
                Toast.makeText(this, "Error getting class list: $exception", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupChildrenSpinner(wardList: List<String>) {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, wardList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        childrenSpinner.adapter = spinnerAdapter

        childrenSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedWard = wardList[position]
                    // Extract schoolId and className from the selected ward text
                    val parts = selectedWard?.split(" - ")
                    val schoolId = parts?.getOrNull(1)?.substringAfter("School: ")?.trim()
                    val className = parts?.getOrNull(2)?.substringAfter("Class: ")?.trim()

                    // If both schoolId and className are extracted, call loadHomeworkforwholeclass
                    if (schoolId != null && className != null) {
                        loadHomeworkforwholeclass(schoolId, className)
                    }
                } else {
                    selectedWard = null
                    // Optionally, you can clear the homework list if no ward is selected
                    recyclerView.adapter = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedWard = null
                recyclerView.adapter = null // Clear the homework list
            }
        }
    }


    private fun loadHomework() {
        if (role == "Parent" && selectedWard == null) {
            recyclerView.adapter = null
            return
        } else if (role != "Parent" && selectedClass == null) {
            recyclerView.adapter = null
            return
        }

        val homeworkList = listOf(
            HomeworkItem("Algebra", "Solve quadratic equations page 45"),
            HomeworkItem("Geometry", "Complete triangle proofs worksheet"),
            HomeworkItem("Biology", "Study cell structure diagrams"),
            HomeworkItem("Chemistry", "Complete periodic table exercises"),
            HomeworkItem("World War II", "Write essay on D-Day invasion"),
            HomeworkItem("Ancient Rome", "Prepare presentation about Roman government")
        )

        homeworkAdapter = HomeworkAdapter(homeworkList, role!!)
        recyclerView.adapter = homeworkAdapter
    }

    private fun loadHomeworkforwholeclass(schoolId: String, className: String) {
        if (className.isEmpty()) {
            recyclerView.adapter = null
            return
        }

        val db = FirebaseFirestore.getInstance()
        val homeworkList = mutableListOf<HomeworkItem>() // Replace HomeworkItem with your actual model

        db.collection("Schools")
            .document(schoolId) // Use dynamic school ID
            .collection("Classes")
            .document(className) // Use passed class name
            .collection("homework") // Fetch all subjects' homework
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val subject = document.id // Subject name (e.g., English, Math, Science)
                    val content = document.getString("content") ?: "No content available"
                    homeworkList.add(HomeworkItem(subject, content)) // Adjust HomeworkItem accordingly
                }

                if (homeworkList.isEmpty()) {
                    Toast.makeText(this, "No homework found for class $className", Toast.LENGTH_SHORT).show()
                }

                homeworkAdapter = HomeworkAdapter(homeworkList, role!!)
                recyclerView.adapter = homeworkAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("HomeworkActivity", "Error loading homework: $exception")
                Toast.makeText(this, "Error loading homework", Toast.LENGTH_SHORT).show()
            }
    }



}