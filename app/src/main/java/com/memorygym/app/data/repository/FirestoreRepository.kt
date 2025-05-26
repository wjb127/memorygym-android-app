package com.memorygym.app.data.repository

import com.memorygym.app.data.model.*
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    
    // Auth operations
    fun getCurrentUserId(): String?
    
    // User operations
    suspend fun createUser(user: User): Result<Unit>
    suspend fun getUser(userId: String): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    
    // Subject operations
    suspend fun createSubject(subject: Subject): Result<String>
    suspend fun getSubjects(userId: String): Flow<List<Subject>>
    suspend fun getSubject(subjectId: String): Result<Subject?>
    suspend fun updateSubject(subject: Subject): Result<Unit>
    suspend fun deleteSubject(subjectId: String): Result<Unit>
    
    // Flashcard operations
    suspend fun createFlashcard(flashcard: Flashcard): Result<String>
    suspend fun getFlashcards(userId: String): Flow<List<Flashcard>>
    suspend fun getFlashcardsBySubject(userId: String, subjectId: String): Flow<List<Flashcard>>
    suspend fun getFlashcard(cardId: String): Result<Flashcard?>
    suspend fun updateFlashcard(flashcard: Flashcard): Result<Unit>
    suspend fun deleteFlashcard(cardId: String): Result<Unit>
    suspend fun getFlashcardsForReview(userId: String): Flow<List<Flashcard>>
    
    // Study Session operations
    suspend fun createStudySession(studySession: StudySession): String
    suspend fun updateStudySession(studySession: StudySession)
    suspend fun getStudySessionsByUser(userId: String): List<StudySession>
    suspend fun getStudySessionsBySubject(subjectId: String): List<StudySession>
    
    // Review Interval operations
    suspend fun getReviewIntervals(): Flow<List<ReviewInterval>>
    
    // Feedback operations
    suspend fun createFeedback(feedback: Feedback): String
    
    // Initial data operations
    suspend fun createInitialDataForUser(userId: String): Result<Unit>
    suspend fun hasInitialData(userId: String): Result<Boolean>
} 