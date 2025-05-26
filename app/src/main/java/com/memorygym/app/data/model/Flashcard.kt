package com.memorygym.app.data.model

import com.google.firebase.Timestamp

data class Flashcard(
    val id: String = "",
    val front: String = "",
    val back: String = "",
    val boxNumber: Int = 1,
    val lastReviewed: Timestamp = Timestamp.now(),
    val nextReview: Timestamp = Timestamp.now(),
    val isAdminCard: Boolean = false,
    val subjectId: String = "",
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val difficulty: Int = 1,
    val reviewCount: Int = 0
) {
    companion object {
        const val MAX_BOX_NUMBER = 5 // 최대 박스 번호 (5단계 시스템)
        const val MIN_BOX_NUMBER = 1 // 최소 박스 번호
    }
    
    /**
     * 정답일 때 다음 박스로 이동
     */
    fun moveToNextBox(): Flashcard {
        val nextBox = if (boxNumber < MAX_BOX_NUMBER) boxNumber + 1 else MAX_BOX_NUMBER
        return copy(
            boxNumber = nextBox,
            lastReviewed = Timestamp.now(),
            reviewCount = reviewCount + 1
        )
    }
    
    /**
     * 오답일 때 첫 번째 박스로 이동
     */
    fun moveToFirstBox(): Flashcard {
        return copy(
            boxNumber = MIN_BOX_NUMBER,
            lastReviewed = Timestamp.now(),
            reviewCount = reviewCount + 1
        )
    }
} 