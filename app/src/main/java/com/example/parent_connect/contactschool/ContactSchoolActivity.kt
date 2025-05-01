package com.example.parent_connect.contactschool

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactSchoolActivity : AppCompatActivity() {

    private lateinit var contactAdapter: ContactSchoolAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserUid: String
    private lateinit var addIconButton: AppCompatImageButton  // Reference to the add button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_school)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.contactSchoolRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firestore and Add Icon Button
        db = FirebaseFirestore.getInstance()
        addIconButton = findViewById(R.id.addIconButton)

        // Get current user UID
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Fetch role and schoolId
        fetchUserRole()
    }

    private fun fetchUserRole() {
        if (currentUserUid.isNotEmpty()) {
            // Firestore reference to fetch user data based on UID
            db.collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val role = documentSnapshot.getString("role") ?: "N/A"

                        // Log the fetched role
                        Log.d("UserRole", "Role: $role")

                        // Conditionally show the add button if the role is SchoolAdmin
                        if (role == "SchoolAdmin") {
                            addIconButton.visibility = View.VISIBLE
                            fetchSchoolId() // Fetch schoolId only if role is SchoolAdmin
                        } else if (role == "Parent") {
                            addIconButton.visibility = View.GONE
                            Log.d("UserRole", "Parent role detected")
                            fetchWardsSchoolIds()
                        } else {
                            addIconButton.visibility = View.GONE
                            fetchSchoolId() // Fetch schoolId if role is not Parent or SchoolAdmin
                        }
                    } else {
                        Log.d("UserRole", "No user data found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserRole", "Error fetching user role: $exception")
                }
        } else {
            Log.d("UserRole", "No user UID available")
        }
    }

    private fun fetchSchoolId() {
        if (currentUserUid.isNotEmpty()) {
            // Firestore reference to fetch schoolId based on UID
            db.collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val schoolIds = mutableListOf<String>()
                        val schoolId = documentSnapshot.getString("schoolId") ?: "N/A"
                        schoolIds.add(schoolId)

                        // Log the fetched schoolId
                        Log.d("UserSchoolId", "SchoolId: $schoolId")

                        // Fetch contact details using the schoolId
                        fetchContactDetails(schoolIds)
                    } else {
                        Log.d("UserSchoolId", "School ID not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserSchoolId", "Error fetching schoolId: $exception")
                }
        }
    }

    private fun fetchWardsSchoolIds() {
        if (currentUserUid.isNotEmpty()) {
            // Firestore reference to fetch wards data for the user
            db.collection("users")
                .document(currentUserUid)
                .collection("Wards")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val schoolIds = mutableListOf<String>()

                    // Loop through each ward document
                    for (document in querySnapshot.documents) {
                        val schoolId = document.getString("schoolId") ?: "N/A"
                        schoolIds.add(schoolId)

                        // Log the fetched schoolId for each ward
                        Log.d("WardSchoolId", "Ward SchoolId: $schoolId")
                    }

                    // After fetching all ward schoolIds, fetch contact details for each schoolId
                    if (schoolIds.isNotEmpty()) {
                        fetchContactDetails(schoolIds)
                    } else {
                        Log.d("WardSchoolId", "No wards found or no schoolId present")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("WardSchoolId", "Error fetching wards data: $exception")
                }
        }
    }

    private fun fetchContactDetails(schoolIds: List<String>) {
        val schoolList = mutableListOf<School>()
        var pendingRequests = schoolIds.size

        // Log the list of schoolIds before fetching contact details
        Log.d("fetchContactDetails", "Fetching contact details for schoolIds: $schoolIds")

        // Loop through each schoolId and fetch both school details and contact details
        for (schoolId in schoolIds) {
            // Fetch the school name and address from the school document
            db.collection("Schools")
                .document(schoolId)
                .get() // Fetch school data (schoolname, schoolAdress)
                .addOnSuccessListener { schoolDocumentSnapshot ->
                    if (schoolDocumentSnapshot.exists()) {
                        // Fetch school name and address
                        val schoolName = schoolDocumentSnapshot.getString("schoolname") ?: "N/A"
                        val schoolAddress = schoolDocumentSnapshot.getString("schoolAdress") ?: "N/A"

                        // Now fetch contact details from AboutSchool/contact_details
                        db.collection("Schools")
                            .document(schoolId)
                            .collection("AboutSchool")
                            .document("contact_details")
                            .get()
                            .addOnSuccessListener { contactDocumentSnapshot ->
                                if (contactDocumentSnapshot.exists()) {
                                    // Fetch contact details (principal, fees, etc.)
                                    val principalPhone = contactDocumentSnapshot.getString("Principles_number") ?: "N/A"
                                    val principalEmail = contactDocumentSnapshot.getString("Principles_email") ?: "N/A"
                                    val feesPhone = contactDocumentSnapshot.getString("FeeSection_number") ?: "N/A"
                                    val feesEmail = contactDocumentSnapshot.getString("FeeSection_email") ?: "N/A"
                                    val schoolPhone = contactDocumentSnapshot.getString("General_number") ?: "N/A"
                                    val schoolEmail = contactDocumentSnapshot.getString("General_email") ?: "N/A"

                                    Log.d("fetchContactDetails", "Fetched values: $schoolName, $schoolAddress, $principalPhone, $principalEmail, $feesPhone, $feesEmail, $schoolPhone, $schoolEmail")

                                    // Add the fetched data to the list
                                    schoolList.add(
                                        School(
                                            name = schoolName,  // Fetched school name
                                            address = schoolAddress,  // Fetched school address
                                            principalPhone = principalPhone,
                                            principalEmail = principalEmail,
                                            feesPhone = feesPhone,
                                            feesEmail = feesEmail,
                                            schoolPhone = schoolPhone,
                                            schoolEmail = schoolEmail
                                        )
                                    )
                                } else {
                                    Log.d("fetchContactDetails", "No contact details found for schoolId: $schoolId")
                                }

                                // Decrease pendingRequests counter
                                pendingRequests--

                                // When all requests are completed, update RecyclerView
                                if (pendingRequests == 0) {
                                    if (schoolList.isNotEmpty()) {
                                        Log.d("fetchContactDetails", "All contact details fetched, setting adapter.")
                                        contactAdapter = ContactSchoolAdapter(schoolList)
                                        recyclerView.adapter = contactAdapter
                                        contactAdapter.notifyDataSetChanged()  // Notify the adapter of changes
                                    } else {
                                        Log.d("fetchContactDetails", "No school contact details found")
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("fetchContactDetails", "Error fetching contact details for schoolId: $schoolId", exception)
                            }
                    } else {
                        Log.d("fetchContactDetails", "No school data found for schoolId: $schoolId")
                        pendingRequests-- // If no school data found, decrement pendingRequests
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("fetchContactDetails", "Error fetching school data for schoolId: $schoolId", exception)
                    pendingRequests-- // If error fetching school data, decrement pendingRequests
                }
        }
    }


}
