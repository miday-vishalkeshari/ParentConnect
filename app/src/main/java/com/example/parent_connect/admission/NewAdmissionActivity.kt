package com.example.parent_connect.admission

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.parent_connect.R

class NewAdmissionActivity : AppCompatActivity() {

    private lateinit var editTextStudentName: EditText
    private lateinit var editTextStudentGrade: EditText
    private lateinit var btnSaveAdmission: Button
    private lateinit var btnExistingParent: Button
    private lateinit var btnNewParentRegistration: Button
    private lateinit var editTextExistingParentId: EditText
    private lateinit var cardExistingParentId: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_admission)

        // Initialize UI components
        editTextStudentName = findViewById(R.id.editTextStudentName)
        editTextStudentGrade = findViewById(R.id.editTextStudentClass)
        btnSaveAdmission = findViewById(R.id.btnSaveAdmission)
        btnExistingParent = findViewById(R.id.btnExistingParent)
        editTextExistingParentId = findViewById(R.id.existingParentId)
        btnNewParentRegistration = findViewById(R.id.btnNewParentRegistration)
        cardExistingParentId = findViewById(R.id.cardExistingParentId)

        // Set click listener for the "Save Admission" button
        btnSaveAdmission.setOnClickListener {
            val studentName = editTextStudentName.text.toString()
            val studentGrade = editTextStudentGrade.text.toString()

            if (studentName.isNotBlank() && studentGrade.isNotBlank()) {
                // Save the new admission (e.g., to a database or send it back as result)
                finish() // Close the activity
            } else {
                // Show error message if fields are blank
                editTextStudentName.error = "Student name is required"
                editTextStudentGrade.error = "Student grade is required"
            }
        }


        // Reference the root layout (LinearLayout or any other root layout in your XML)
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout) // Replace with your root layout ID


        // Set click listener for the "Existing Parent" button
        btnNewParentRegistration.setOnClickListener {
            cardExistingParentId.visibility = View.GONE
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_light))
        }


        // Set click listener for the "Existing Parent" button
        btnExistingParent.setOnClickListener {
            cardExistingParentId.visibility = View.VISIBLE
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_light)) // Replace R.color.pink with your pink color resource
        }
    }
}
