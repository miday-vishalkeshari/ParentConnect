package com.example.parent_connect.event


import java.io.Serializable

data class Event(
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val schoolName: String = ""
) : Serializable {  // Implement Serializable interface
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "")
}
