package com.memorygym.app.data.model

import com.google.firebase.Timestamp

data class Feedback(
    val id: String = "",
    val content: String = "",
    val email: String? = null,
    val userId: String? = null,
    val createdAt: Timestamp = Timestamp.now()
) 