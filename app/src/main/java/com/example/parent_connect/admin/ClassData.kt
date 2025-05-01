package com.example.parent_connect.admin

data class ClassData(
    val classId: String,
    val sections: List<String> = emptyList()  // Add a list of sections for each class
)
