package com.example.parent_connect.event

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R

class AddEventActivity : AppCompatActivity() {

    private lateinit var eventTitle: EditText
    private lateinit var eventDate: EditText
    private lateinit var eventDescription: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // Initialize views
        eventTitle = findViewById(R.id.eventTitle)
        eventDate = findViewById(R.id.eventDate)
        eventDescription = findViewById(R.id.eventDescription)

        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            // Get event data from input fields
            val title = eventTitle.text.toString().trim()
            val date = eventDate.text.toString().trim()
            val description = eventDescription.text.toString().trim()


            // Validate input
            if (title.isNotEmpty() && date.isNotEmpty() && description.isNotEmpty() ) {
                // Create an event object
                val event = Event(
                    title = title,
                    date = date,
                    description = description,
                    schoolName = "school name1 hardcoded"
                )

                // Return the event to EventsActivity
                val resultIntent = Intent()
                val bundle = Bundle()
                bundle.putSerializable("NEW_EVENT", event)  // Pass the event using Serializable
                resultIntent.putExtras(bundle)
                setResult(RESULT_OK, resultIntent)
                finish()  // Close this activity and return to the previous one
            } else {
                // Show error if any field is empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
