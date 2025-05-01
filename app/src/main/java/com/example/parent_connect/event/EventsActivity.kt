package com.example.parent_connect.event

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.database.*

class EventsActivity : AppCompatActivity() {

    private lateinit var recyclerEvents: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var eventList: MutableList<Event>
    private lateinit var database: DatabaseReference

    // Register for a result from AddEventActivity
    private val addEventLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newEvent: Event? = result.data?.getSerializableExtra("NEW_EVENT") as? Event
                newEvent?.let {
                    // Add the new event to the list and update the RecyclerView
                    eventList.add(it)
                    eventAdapter.notifyItemInserted(eventList.size - 1)
                    Toast.makeText(this, "New event added!", Toast.LENGTH_SHORT).show()

                    // Push the new event to Firebase
                    pushEventToFirebase(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        recyclerEvents = findViewById(R.id.recyclerEvents)
        recyclerEvents.layoutManager = LinearLayoutManager(this)

        // Buttons
        val schoolEvents = findViewById<Button>(R.id.leftButton)
        val socialEvents = findViewById<Button>(R.id.rightButton)

        // Titles for the sections
        val EventsTitle = findViewById<TextView>(R.id.EventsTitle)




        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize the event list
        eventList = mutableListOf()

        // Get the user role from the intent
        val role = intent.getStringExtra("USER_ROLE") ?: ""

        // Initialize the adapter with the user role
        eventAdapter = EventAdapter(eventList, role) { event ->
            deleteEventFromFirebase(event)
        }
        recyclerEvents.adapter = eventAdapter

        // Fetch events from Firebase when the activity starts
        fetchEventsFromFirebase()

        // Check if the user is a School Admin
        Log.d("EventsActivity", "User Role from Intent: $role")

        val addEventButton = findViewById<View>(R.id.addEventButton)

        if (role == "SchoolAdmin") {
            socialEvents.visibility=View.GONE
            schoolEvents.visibility=View.GONE
            // Show the "Add New Event" button if the user is School Admin
            addEventButton.visibility = View.VISIBLE
            Log.d("EventsActivity", "Role is SchoolAdmin, Add Event Button is visible")

            // Handle add new event button click
            addEventButton.setOnClickListener {
                Log.d("EventsActivity", "Add Event button clicked")
                // Start AddEventActivity to add a new event
                val intent = Intent(this, AddEventActivity::class.java)
                addEventLauncher.launch(intent)
            }
        }
        else if(role=="Teacher")
        {
            socialEvents.visibility=View.GONE
            schoolEvents.visibility=View.GONE
            addEventButton.visibility = View.GONE
        }
        else {
            // Hide the "Add New Event" button if the user is not School Admin
            addEventButton.visibility = View.GONE
            Log.d("EventsActivity", "Role is not SchoolAdmin, Add Event Button is hidden")
        }
    }

    // Function to fetch events from Firebase
    private fun fetchEventsFromFirebase() {
        Log.d("EventsActivity", "Fetching events from Firebase...")
        database.child("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("EventsActivity", "Events fetched successfully")
                eventList.clear() // Clear the list before adding updated data
                for (dataSnapshot in snapshot.children) {
                    val event = dataSnapshot.getValue(Event::class.java)
                    event?.let {
                        eventList.add(it)
                    }
                }
                // Notify the adapter that the data has changed
                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventsActivity", "Failed to load events: ${error.message}")
                Toast.makeText(this@EventsActivity, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to push new event to Firebase
    private fun pushEventToFirebase(event: Event) {
        Log.d("EventsActivity", "Pushing new event to Firebase...")
        val eventId = database.child("events").push().key
        if (eventId != null) {
            database.child("events").child(eventId).setValue(event)
                .addOnSuccessListener {
                    Log.d("EventsActivity", "Event successfully added to Firebase")
                    Toast.makeText(this, "Event successfully added to Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("EventsActivity", "Failed to add event to Firebase")
                    Toast.makeText(this, "Failed to add event to Firebase", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("EventsActivity", "Failed to generate a unique event ID")
        }
    }

    // Function to delete an event from Firebase
    private fun deleteEventFromFirebase(event: Event) {
        Log.d("EventsActivity", "Deleting event from Firebase...")
        database.child("events").orderByChild("title").equalTo(event.title)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        dataSnapshot.ref.removeValue().addOnSuccessListener {
                            Log.d("EventsActivity", "Event successfully deleted")
                            Toast.makeText(this@EventsActivity, "Event deleted", Toast.LENGTH_SHORT).show()
                            eventList.remove(event)
                            eventAdapter.notifyDataSetChanged()
                        }.addOnFailureListener {
                            Log.e("EventsActivity", "Failed to delete event")
                            Toast.makeText(this@EventsActivity, "Failed to delete event", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EventsActivity", "Failed to delete event: ${error.message}")
                }
            })
    }
}
