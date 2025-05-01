package com.example.parent_connect.holidays

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class HolidaysActivity : AppCompatActivity() {

    private lateinit var holidayAdapter: HolidayAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holidays)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.holidaysRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample holiday data
        val holidayList = listOf(
            Holiday(
                title = "New Year",
                date = "1st January",
                description = "Celebration of the New Year.",
                schoolName = "Alisa - ABC International School"
            ),
            Holiday(
                title = "Independence Day",
                date = "15th August",
                description = "Commemorates independence.",
                schoolName = "Mary - XYZ High School"
            ),
            Holiday(
                title = "Christmas",
                date = "25th December",
                description = "Festival celebrating the birth of Jesus.",
                schoolName = "John - ABC International School"
            )
        )

        // Set up adapter
        holidayAdapter = HolidayAdapter(holidayList)
        recyclerView.adapter = holidayAdapter
    }
}
