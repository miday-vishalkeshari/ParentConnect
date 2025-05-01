package com.example.parent_connect.homework

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import com.google.firebase.firestore.FirebaseFirestore

class AddHomeworkActivity : AppCompatActivity() {

    private lateinit var subjectEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitHomeworkButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_homework)

        // Get the schoolId and selectedClass from the Intent extras
        val schoolId = intent.getStringExtra("SCHOOL_ID") ?: ""
        val selectedClass = intent.getStringExtra("CLASS_NAME") ?: ""

        subjectEditText = findViewById(R.id.subjectEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        submitHomeworkButton = findViewById(R.id.submitHomeworkButton)

        submitHomeworkButton.setOnClickListener {
            addHomeworkToFirebase(schoolId, selectedClass)  // Pass schoolId and selectedClass
        }
    }

    private fun addHomeworkToFirebase(schoolId: String, selectedClass: String) {
        val subject = subjectEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        // Ensure subject, description, and selectedClass are not empty
        if (subject.isEmpty() || description.isEmpty() || selectedClass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val homeworkData = hashMapOf(
            "content" to description
        )

        // Use schoolId and selectedClass to dynamically set the document paths
        db.collection("Schools")
            .document(schoolId) // Use the schoolId passed
            .collection("Classes")
            .document(selectedClass) // Use the selectedClass passed
            .collection("homework")
            .document(subject) // Use subject as the document ID
            .set(homeworkData)
            .addOnSuccessListener {
                Toast.makeText(this, "Homework added successfully", Toast.LENGTH_SHORT).show()

                // Return result to HomeworkActivity
                setResult(RESULT_OK)
                finish() // Close activity after successful submission
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
