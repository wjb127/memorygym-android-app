package com.memorygym.app.data.model

import com.google.firebase.Timestamp

data class Subject(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val cardCount: Int = 0,
    val lastStudied: Timestamp? = null
) 