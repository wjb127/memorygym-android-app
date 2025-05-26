package com.memorygym.app.data.model

import com.google.firebase.Timestamp

data class StudySession(
    val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp? = null,
    val totalCards: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val completedAt: Timestamp? = null
) 