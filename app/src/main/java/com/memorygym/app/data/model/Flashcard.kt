package com.memorygym.app.data.model

import com.google.firebase.Timestamp
import java.util.Calendar

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
        
        // 각 상자별 복습 간격 (일) - Next.js와 동일
        private val REVIEW_INTERVALS = arrayOf(1, 3, 7, 14, 30) // 1일, 3일, 7일, 14일, 30일
    }
    
    /**
     * 정답일 때 다음 박스로 이동
     */
    fun moveToNextBox(): Flashcard {
        val nextBox = if (boxNumber < MAX_BOX_NUMBER) boxNumber + 1 else MAX_BOX_NUMBER
        val nextReviewDate = calculateNextReviewDate(nextBox)
        
        return copy(
            boxNumber = nextBox,
            lastReviewed = Timestamp.now(),
            nextReview = nextReviewDate,
            reviewCount = reviewCount + 1
        )
    }
    
    /**
     * 오답일 때 첫 번째 박스로 이동
     */
    fun moveToFirstBox(): Flashcard {
        val nextReviewDate = calculateNextReviewDate(MIN_BOX_NUMBER)
        
        return copy(
            boxNumber = MIN_BOX_NUMBER,
            lastReviewed = Timestamp.now(),
            nextReview = nextReviewDate,
            reviewCount = reviewCount + 1
        )
    }
    
    /**
     * 다음 복습 날짜 계산 (Next.js 로직과 동일)
     */
    private fun calculateNextReviewDate(boxNumber: Int): Timestamp {
        val calendar = Calendar.getInstance()
        val intervalDays = REVIEW_INTERVALS[boxNumber - 1] // 0-based 인덱스
        calendar.add(Calendar.DAY_OF_MONTH, intervalDays)
        return Timestamp(calendar.time)
    }
} 