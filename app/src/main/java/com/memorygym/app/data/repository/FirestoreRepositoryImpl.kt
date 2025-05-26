package com.memorygym.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.memorygym.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {

    override fun getCurrentUserId(): String? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val SUBJECTS_COLLECTION = "subjects"
        private const val FLASHCARDS_COLLECTION = "flashcards"
        private const val STUDY_SESSIONS_COLLECTION = "studySessions"
        private const val REVIEW_INTERVALS_COLLECTION = "reviewIntervals"
        private const val FEEDBACK_COLLECTION = "feedback"
    }

    // User operations
    override suspend fun createUser(user: User): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val user = document.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val updatedUser = user.copy(updatedAt = Timestamp.now())
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(updatedUser)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Subject operations
    override suspend fun createSubject(subject: Subject): Result<String> {
        return try {
            val docRef = firestore.collection(SUBJECTS_COLLECTION).document()
            val subjectWithId = subject.copy(id = docRef.id)
            docRef.set(subjectWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubjects(userId: String): Flow<List<Subject>> = callbackFlow {
        val listener = firestore.collection(SUBJECTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val subjects = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Subject::class.java)
                } ?: emptyList()
                
                // 클라이언트 사이드에서 정렬
                val sortedSubjects = subjects.sortedByDescending { it.createdAt }
                
                trySend(sortedSubjects)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getSubject(subjectId: String): Result<Subject?> {
        return try {
            val document = firestore.collection(SUBJECTS_COLLECTION)
                .document(subjectId)
                .get()
                .await()
            
            val subject = document.toObject(Subject::class.java)
            Result.success(subject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubject(subject: Subject): Result<Unit> {
        return try {
            firestore.collection(SUBJECTS_COLLECTION)
                .document(subject.id)
                .set(subject)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubject(subjectId: String): Result<Unit> {
        return try {
            firestore.collection(SUBJECTS_COLLECTION)
                .document(subjectId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Flashcard operations
    override suspend fun createFlashcard(flashcard: Flashcard): Result<String> {
        return try {
            val docRef = firestore.collection(FLASHCARDS_COLLECTION).document()
            val flashcardWithId = flashcard.copy(id = docRef.id)
            docRef.set(flashcardWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFlashcards(userId: String): Flow<List<Flashcard>> = callbackFlow {
        val listener = firestore.collection(FLASHCARDS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val flashcards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Flashcard::class.java)
                } ?: emptyList()
                
                val sortedFlashcards = flashcards.sortedByDescending { it.createdAt }
                
                trySend(sortedFlashcards)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getFlashcardsBySubject(userId: String, subjectId: String): Flow<List<Flashcard>> = callbackFlow {
        val listener = firestore.collection(FLASHCARDS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("subjectId", subjectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val flashcards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Flashcard::class.java)
                } ?: emptyList()
                
                val sortedFlashcards = flashcards.sortedByDescending { it.createdAt }
                
                trySend(sortedFlashcards)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getFlashcard(cardId: String): Result<Flashcard?> {
        return try {
            val document = firestore.collection(FLASHCARDS_COLLECTION)
                .document(cardId)
                .get()
                .await()
            
            val flashcard = document.toObject(Flashcard::class.java)
            Result.success(flashcard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFlashcard(flashcard: Flashcard): Result<Unit> {
        return try {
            firestore.collection(FLASHCARDS_COLLECTION)
                .document(flashcard.id)
                .set(flashcard)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFlashcard(cardId: String): Result<Unit> {
        return try {
            firestore.collection(FLASHCARDS_COLLECTION)
                .document(cardId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFlashcardsForReview(userId: String): Flow<List<Flashcard>> = callbackFlow {
        val listener = firestore.collection(FLASHCARDS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereLessThanOrEqualTo("nextReview", Timestamp.now())
            .orderBy("nextReview", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val flashcards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Flashcard::class.java)
                } ?: emptyList()
                
                trySend(flashcards)
            }
        
        awaitClose { listener.remove() }
    }

    // Study Session operations
    override suspend fun createStudySession(studySession: StudySession): String {
        return try {
            val docRef = firestore.collection("studySessions").document(studySession.id)
            docRef.set(studySession).await()
            studySession.id
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateStudySession(studySession: StudySession) {
        try {
            firestore.collection("studySessions")
                .document(studySession.id)
                .set(studySession)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getStudySessionsByUser(userId: String): List<StudySession> {
        return try {
            firestore.collection("studySessions")
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(StudySession::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getStudySessionsBySubject(subjectId: String): List<StudySession> {
        return try {
            firestore.collection("studySessions")
                .whereEqualTo("subjectId", subjectId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(StudySession::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Review Interval operations
    override suspend fun getReviewIntervals(): Flow<List<ReviewInterval>> = callbackFlow {
        val listener = firestore.collection(REVIEW_INTERVALS_COLLECTION)
            .orderBy("boxNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val intervals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ReviewInterval::class.java)
                } ?: emptyList()
                
                trySend(intervals)
            }
        
        awaitClose { listener.remove() }
    }

    // Feedback operations
    override suspend fun createFeedback(feedback: Feedback): String {
        return try {
            val docRef = firestore.collection("feedback").document()
            val feedbackWithId = feedback.copy(id = docRef.id)
            docRef.set(feedbackWithId).await()
            docRef.id
        } catch (e: Exception) {
            throw e
        }
    }
} 