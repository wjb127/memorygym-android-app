package com.memorygym.app.presentation.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.memorygym.app.data.model.Flashcard
import com.memorygym.app.data.model.StudySession
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StudyUiState(
    val isLoading: Boolean = false,
    val currentCard: Flashcard? = null,
    val remainingCards: List<Flashcard> = emptyList(),
    val progress: Int = 0,
    val totalCards: Int = 0,
    val error: String? = null
)

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var studySessionId: String? = null
    private var correctCount = 0
    private var incorrectCount = 0

    fun loadFlashcardsForStudy(subjectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = repository.getCurrentUserId() ?: return@launch
                val allCards = mutableListOf<Flashcard>()
                
                // Flow를 collect하여 카드 목록 가져오기
                repository.getFlashcardsBySubject(userId, subjectId).collect { cards ->
                    allCards.clear()
                    allCards.addAll(cards)
                }
                
                val now = Timestamp.now()
                
                // 복습할 카드들 필터링 (nextReview가 현재 시간보다 이전인 카드들)
                val cardsToStudy = allCards.filter { card ->
                    card.nextReview?.let { nextReview ->
                        nextReview.seconds <= now.seconds
                    } ?: true // nextReview가 null이면 학습 대상
                }
                
                if (cardsToStudy.isNotEmpty()) {
                    // 새로운 학습 세션 시작
                    startNewStudySession(subjectId, cardsToStudy.size)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentCard = cardsToStudy.first(),
                        remainingCards = cardsToStudy.drop(1),
                        progress = 1,
                        totalCards = cardsToStudy.size
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentCard = null,
                        remainingCards = emptyList(),
                        progress = 0,
                        totalCards = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "카드를 불러오는데 실패했습니다: ${e.message}"
                )
            }
        }
    }

    fun markAsCorrect(flashcard: Flashcard) {
        viewModelScope.launch {
            try {
                correctCount++
                
                // 카드를 다음 박스로 이동
                val updatedCard = flashcard.moveToNextBox()
                repository.updateFlashcard(updatedCard)
                
                moveToNextCard()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "카드 업데이트에 실패했습니다: ${e.message}"
                )
            }
        }
    }

    fun markAsIncorrect(flashcard: Flashcard) {
        viewModelScope.launch {
            try {
                incorrectCount++
                
                // 카드를 첫 번째 박스로 이동
                val updatedCard = flashcard.moveToFirstBox()
                repository.updateFlashcard(updatedCard)
                
                moveToNextCard()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "카드 업데이트에 실패했습니다: ${e.message}"
                )
            }
        }
    }

    private fun moveToNextCard() {
        val currentState = _uiState.value
        
        if (currentState.remainingCards.isNotEmpty()) {
            _uiState.value = currentState.copy(
                currentCard = currentState.remainingCards.first(),
                remainingCards = currentState.remainingCards.drop(1),
                progress = currentState.progress + 1
            )
        } else {
            // 모든 카드 완료
            _uiState.value = currentState.copy(
                currentCard = null,
                remainingCards = emptyList(),
                progress = currentState.totalCards
            )
            
            // 학습 세션 완료
            completeStudySession()
        }
    }

    private suspend fun startNewStudySession(subjectId: String, totalCards: Int) {
        try {
            val studySession = StudySession(
                id = UUID.randomUUID().toString(),
                userId = repository.getCurrentUserId() ?: "",
                subjectId = subjectId,
                startTime = Timestamp.now(),
                endTime = null,
                totalCards = totalCards,
                correctAnswers = 0,
                incorrectAnswers = 0,
                completedAt = null
            )
            
            repository.createStudySession(studySession)
            studySessionId = studySession.id
            correctCount = 0
            incorrectCount = 0
        } catch (e: Exception) {
            // 세션 생성 실패해도 학습은 계속 진행
            println("Failed to create study session: ${e.message}")
        }
    }

    private fun completeStudySession() {
        viewModelScope.launch {
            studySessionId?.let { sessionId ->
                try {
                    val updatedSession = StudySession(
                        id = sessionId,
                        userId = repository.getCurrentUserId() ?: "",
                        subjectId = "", // 실제로는 저장된 값 사용
                        startTime = Timestamp.now(), // 실제로는 저장된 값 사용
                        endTime = Timestamp.now(),
                        totalCards = _uiState.value.totalCards,
                        correctAnswers = correctCount,
                        incorrectAnswers = incorrectCount,
                        completedAt = Timestamp.now()
                    )
                    
                    repository.updateStudySession(updatedSession)
                } catch (e: Exception) {
                    println("Failed to complete study session: ${e.message}")
                }
            }
        }
    }
} 