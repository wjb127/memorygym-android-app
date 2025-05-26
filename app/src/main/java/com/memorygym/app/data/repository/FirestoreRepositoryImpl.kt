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

    // Initial data operations
    override suspend fun createInitialDataForUser(userId: String): Result<Unit> {
        return try {
            // 중급 영단어 과목 생성
            val subject = Subject(
                id = "",
                userId = userId,
                name = "중급 영단어",
                description = "일상생활과 업무에서 자주 사용되는 중급 수준의 영단어 모음",
                createdAt = Timestamp.now()
            )
            
            val subjectResult = createSubject(subject)
            if (subjectResult.isFailure) {
                return Result.failure(subjectResult.exceptionOrNull() ?: Exception("과목 생성 실패"))
            }
            
            val subjectId = subjectResult.getOrThrow()
            
            // 중급 영단어 50개 데이터
            val vocabularyData = listOf(
                "accomplish" to "성취하다, 완수하다",
                "adequate" to "적절한, 충분한",
                "analyze" to "분석하다",
                "approach" to "접근하다, 방법",
                "appropriate" to "적절한, 알맞은",
                "assess" to "평가하다, 사정하다",
                "assume" to "가정하다, 추정하다",
                "benefit" to "이익, 혜택",
                "challenge" to "도전, 어려움",
                "circumstance" to "상황, 환경",
                "collaborate" to "협력하다",
                "communicate" to "의사소통하다",
                "comprehensive" to "포괄적인, 종합적인",
                "concentrate" to "집중하다",
                "consequence" to "결과, 영향",
                "contribute" to "기여하다, 공헌하다",
                "demonstrate" to "보여주다, 증명하다",
                "determine" to "결정하다, 확정하다",
                "develop" to "개발하다, 발전시키다",
                "distinguish" to "구별하다, 구분하다",
                "efficient" to "효율적인",
                "emphasize" to "강조하다",
                "establish" to "설립하다, 확립하다",
                "evaluate" to "평가하다",
                "evidence" to "증거, 근거",
                "examine" to "조사하다, 검토하다",
                "experience" to "경험, 체험하다",
                "fundamental" to "기본적인, 근본적인",
                "generate" to "생성하다, 만들어내다",
                "identify" to "확인하다, 식별하다",
                "implement" to "실행하다, 구현하다",
                "indicate" to "나타내다, 지시하다",
                "influence" to "영향을 주다",
                "interpret" to "해석하다",
                "investigate" to "조사하다",
                "maintain" to "유지하다, 보존하다",
                "objective" to "목표, 객관적인",
                "obtain" to "얻다, 획득하다",
                "opportunity" to "기회",
                "participate" to "참여하다",
                "perspective" to "관점, 시각",
                "potential" to "잠재력, 가능성",
                "procedure" to "절차, 과정",
                "recognize" to "인식하다, 알아보다",
                "recommend" to "추천하다",
                "require" to "필요로 하다, 요구하다",
                "significant" to "중요한, 의미있는",
                "strategy" to "전략, 계획",
                "sufficient" to "충분한",
                "technique" to "기술, 방법"
            )
            
            // 플래시카드 생성
            vocabularyData.forEach { (english, korean) ->
                val flashcard = Flashcard(
                    id = "",
                    userId = userId,
                    subjectId = subjectId,
                    front = korean,
                    back = english,
                    boxNumber = 1, // 1단계 훈련소에 배치
                    nextReview = Timestamp.now(),
                    createdAt = Timestamp.now()
                )
                
                val flashcardResult = createFlashcard(flashcard)
                if (flashcardResult.isFailure) {
                    return Result.failure(flashcardResult.exceptionOrNull() ?: Exception("플래시카드 생성 실패"))
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasInitialData(userId: String): Result<Boolean> {
        return try {
            val subjects = firestore.collection(SUBJECTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", "중급 영단어")
                .get()
                .await()
            
            Result.success(subjects.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 