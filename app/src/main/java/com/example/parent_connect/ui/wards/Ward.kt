package com.example.parent_connect.ui.wards

data class Ward(
    val schoolId: String = "",
    var classId: String? = null,
    val admissionNo: String = "",
    var wardName: String? = null,
    var profileImageUrl: String? = null // Add this field for the profile image URL
) {
    // No-argument constructor for Firestore
    constructor() : this("", null, "", null, null)
}