package com.example.parent_connect.noticeboard
import java.io.Serializable
data class Notice(
    val title: String = "",
    val description: String = "",
    val date: String = ""
): Serializable
