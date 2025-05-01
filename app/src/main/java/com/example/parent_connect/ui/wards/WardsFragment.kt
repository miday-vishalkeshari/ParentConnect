package com.example.parent_connect.ui.wards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.admin.AdminAdapter
import com.example.parent_connect.admin.ClassData
import com.example.parent_connect.databinding.FragmentWardsBinding
import com.example.parent_connect.teachers_and_students.teacherschedule.TeacherAdapter
import com.example.parent_connect.teachers_and_students.teacherschedule.TeacherScheduleData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WardsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WardAdapter
    private lateinit var db: FirebaseFirestore
    private val wardList = mutableListOf<Ward>() // Class-level list for wards
    private val teacherList = mutableListOf<TeacherScheduleData>() // For teacher-specific data
    private val classList = mutableListOf<ClassData>() // For class data (Admin)
    private var totalWardsToFetch = 0 // Counter for total wards
    private var fetchedWardsCount = 0 // Counter for wards fetched

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWardsBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewWards

        // Reference to TextView
        val titleTextView = binding.textViewTitle

        // Initialize adapter for ward data
        adapter = WardAdapter(wardList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get the user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Fetch the user's role from Firestore to check if the user is a "parent", "teacher", or "admin"
            checkUserRoleAndFetchData(userId,titleTextView)
        } else {
            Log.e("WardsFragment", "User not logged in or userId is null")
        }

        return binding.root
    }

    private fun checkUserRoleAndFetchData(userId: String, titleTextView: TextView) {
        // Fetch the user's role from Firestore
        db.collection("users") // Assuming the users' role is stored in the Users collection
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") // Fetch the role field

                    when (role) {
                        "Parent" -> {
                            titleTextView.text = "Wards Detail"
                            // If role is "parent", proceed to fetch ward data
                            fetchWardData(userId)
                        }
                        "Teacher" -> {
                            titleTextView.text = "Teacher Schedule"
                            // If role is "teacher", proceed to fetch teacher-specific data
                            fetchTeacherData(userId)
                        }
                        else -> {
                            titleTextView.text = "Class Details"
                            // If role is "Admin", proceed to fetch class data (admin role)
                            fetchAdminData(userId)
                        }
                    }
                } else {
                    Log.e("WardsFragment", "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Error fetching user role: ${exception.message}")
            }
    }

    private fun fetchWardData(userId: String) {
        Log.d("WardsFragment", "Using userId: $userId")

        db.collection("users")
            .document(userId)  // Using userId from Firebase Authentication
            .collection("Wards")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("WardsFragment", "Fetched documents count: ${documents.size()}")
                if (!documents.isEmpty) {
                    wardList.clear() // Clear the list before adding new data

                    // Set the total number of wards to fetch
                    totalWardsToFetch = documents.size()

                    // Process each ward document
                    for (document in documents) {
                        val ward = document.toObject(Ward::class.java)
                        // Fetch the ward name for each ward
                        fetchWardNameFromStudent(ward)
                    }
                } else {
                    Log.e("WardsFragment", "No wards found for userId: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Firestore error: ${exception.message}", exception)
            }
    }

    private fun fetchWardNameFromStudent(ward: Ward) {
        Log.d("WardsFragment", "Fetching ward name using schoolId: ${ward.schoolId}, classId: ${ward.classId}, admissionNo: ${ward.admissionNo}")

        db.collection("Schools")
            .document(ward.schoolId)
            .collection("Students")
            .document(ward.admissionNo) // Use admissionNo directly as the document ID
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val wardName = document.getString("name")
                    if (wardName != null) {
                        ward.wardName = wardName
                        Log.d("WardsFragment", "Fetched ward name: $wardName")
                    } else {
                        Log.e("WardsFragment", "Ward name not found in student document")
                    }

                    //ward class
                    val wardClass = document.getString("class")
                    if (wardClass != null) {
                        ward.classId = wardClass
                        Log.d("WardsFragment", "Fetched ward name: $wardClass")
                    } else {
                        Log.e("WardsFragment", "Ward name not found in student document")
                    }

                    // Fetch profile image URL
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (profileImageUrl != null) {
                        ward.profileImageUrl = profileImageUrl
                        Log.d("WardsFragment", "Fetched profile image URL: $profileImageUrl")
                    } else {
                        Log.e("WardsFragment", "Profile image URL not found in student document")
                    }

                    wardList.add(ward)
                    fetchedWardsCount++

                    if (fetchedWardsCount == totalWardsToFetch) {
                        updateRecyclerView()
                    }
                } else {
                    Log.e("WardsFragment", "No student found with admissionNo: ${ward.admissionNo}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Failed to fetch ward name: ${exception.message}", exception)
            }
    }

    // Fetch teacher data from Firestore
    private fun fetchTeacherData(userId: String) {
        Log.d("WardsFragment", "Fetching teacher-specific data for userId: $userId")

        val db = FirebaseFirestore.getInstance()

        // Reference to the teacher's data
        val teacherRef = db.collection("Schools")
            .document("sch1") // School ID (can be dynamic if needed)
            .collection("Teachers")
            .document(userId) // Teacher's UID

        // Fetch data from Firestore
        teacherRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Clear previous data
                    teacherList.clear()

                    // Log the entire document to see its contents
                    Log.d("TeacherData", "Fetched document: ${document.data}")

                    // Fetch and log the 'periods' field
                    val periodsField = document.get("periods")
                    Log.d("TeacherData", "Periods field: $periodsField")

                    // Safely cast the 'periods' field to a Map<String, List<String>>
                    val periodsMap = periodsField as? Map<String, Any> // Cast as Map<String, Any> first
                    Log.d("TeacherData", "Casted periodsMap: $periodsMap")

                    if (periodsMap != null) {
                        periodsMap.forEach { (periodKey, classSubject) ->
                            val periodString = periodKey // periodKey is a String

                            // Check if classSubject is a List, if not wrap it in a list
                            val classSubjectList = when (classSubject) {
                                is List<*> -> classSubject.filterIsInstance<String>() // Ensure it's a list of strings
                                is String -> listOf(classSubject) // If it's a single string, wrap it in a list
                                else -> emptyList() // If neither, create an empty list
                            }

                            if (classSubjectList.size >= 2) {
                                val className = classSubjectList[0] // Class Name
                                val subject = classSubjectList[1] // Subject Name

                                // Create TeacherScheduleData object
                                teacherList.add(TeacherScheduleData("class: $className", "subject: $subject", "period: $periodKey"))


                            } else if (classSubjectList.size == 1) {
                                val className = classSubjectList[0] // Class Name
                                val subject = "" // No subject provided

                                // Create TeacherScheduleData object
                                teacherList.add(TeacherScheduleData("class: $className", "subject: $subject", "period: $periodKey"))

                            }
                        }
                    } else {
                        Log.e("TeacherData", "Failed to cast periods map correctly")
                    }

                    // Update the RecyclerView
                    updateTeacherRecyclerView()
                } else {
                    Log.d("WardsFragment", "No data found for teacher: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Error fetching teacher data: ", exception)
            }
    }




    private fun fetchAdminData(userId: String) {
        Log.d("WardsFragment", "Fetching schoolId for userId: $userId")

        // Fetch the schoolId for the current user
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val schoolId = userDocument.getString("schoolId")
                    if (!schoolId.isNullOrEmpty()) {
                        Log.d("WardsFragment", "Fetched schoolId: $schoolId")

                        // Now fetch class_lists from /Schools/{schoolId}
                        fetchClassListsAndData(schoolId)
                    } else {
                        Log.e("WardsFragment", "schoolId is null or empty for userId: $userId")
                    }
                } else {
                    Log.e("WardsFragment", "User document does not exist for userId: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Error fetching user document: ${exception.message}")
            }
    }

    private fun fetchClassListsAndData(schoolId: String) {
        db.collection("Schools").document(schoolId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val classLists = document.get("class_lists") as? List<String> ?: emptyList()
                    Log.d("WardsFragment", "Fetched class_lists: $classLists for schoolId: $schoolId")

                    // Fetch all documents from the Classes subcollection
                    db.collection("Schools").document(schoolId).collection("Classes").get()
                        .addOnSuccessListener { querySnapshot ->
                            Log.d("WardsFragment", "Fetched ${querySnapshot.size()} class documents for schoolId: $schoolId")

                            val classDataMap = mutableMapOf<String, MutableList<String>>()

                            for (classDoc in querySnapshot.documents) {
                                val className = classDoc.id // e.g., "6A"
                                val baseClassName = className.takeWhile { it.isDigit() } // e.g., "6"

                                Log.d("WardsFragment", "Processing class document: $className, base class name: $baseClassName")

                                if (classLists.contains(baseClassName)) {
                                    val section = className.drop(baseClassName.length) // e.g., "A"
                                    Log.d("WardsFragment", "Adding section $section to class $baseClassName")

                                    if (classDataMap.containsKey(baseClassName)) {
                                        classDataMap[baseClassName]?.add(section)
                                    } else {
                                        classDataMap[baseClassName] = mutableListOf(section)
                                    }
                                } else {
                                    Log.d("WardsFragment", "Skipping class $className as base class name $baseClassName is not in class_lists")
                                }
                            }

                            classList.clear()
                            classDataMap.forEach { (className, sections) ->
                                Log.d("WardsFragment", "Adding class data: $className with sections $sections")
                                classList.add(ClassData(className, sections))
                            }

                            Log.d("WardsFragment", "Final classList size: ${classList.size}")
                            updateAdminRecyclerView()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("WardsFragment", "Error fetching Classes: ${exception.message}")
                        }
                } else {
                    Log.e("WardsFragment", "Document /Schools/$schoolId does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WardsFragment", "Error fetching class_lists for schoolId $schoolId: ${exception.message}")
            }
    }



    private fun updateAdminRecyclerView() {
        // Create and set the adapter for admin data (class data)
        val adminAdapter = AdminAdapter(classList)
        recyclerView.adapter = adminAdapter
    }

    private fun updateRecyclerView() {
        adapter.notifyDataSetChanged()
    }

    private fun updateTeacherRecyclerView() {
        val teacherAdapter = TeacherAdapter(teacherList)
        recyclerView.adapter = teacherAdapter
    }
}
