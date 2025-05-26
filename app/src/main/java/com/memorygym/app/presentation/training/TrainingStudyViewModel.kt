package com.memorygym.app.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorygym.app.data.model.Flashcard
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnswerState {
    WAITING,
    CORRECT,
    INCORRECT
}

data class TrainingStudyUiState(
    val currentCard: Flashcard? = null,
    val currentIndex: Int = 0,
    val totalCards: Int = 0,
    val answerState: AnswerState = AnswerState.WAITING,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
    val remainingCards: List<Flashcard> = emptyList(),
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
)

@HiltViewModel
class TrainingStudyViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingStudyUiState())
    val uiState: StateFlow<TrainingStudyUiState> = _uiState.asStateFlow()

    private var allCards: List<Flashcard> = emptyList()
    private var currentCardIndex = 0

    fun loadCardsForTraining(subjectId: String, trainingLevel: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val userId = repository.getCurrentUserId() ?: return@launch
                
                repository.getFlashcardsBySubject(userId, subjectId).collect { flashcards ->
                    // 해당 훈련소 레벨의 카드들만 필터링
                    val levelCards = flashcards.filter { it.boxNumber == trainingLevel }
                    
                    if (levelCards.isNotEmpty()) {
                        allCards = levelCards.shuffled() // 카드 순서 섞기
                        currentCardIndex = 0
                        
                        _uiState.value = _uiState.value.copy(
                            currentCard = allCards[0],
                            currentIndex = 1,
                            totalCards = allCards.size,
                            answerState = AnswerState.WAITING,
                            isLoading = false,
                            remainingCards = allCards.drop(1)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            currentCard = null,
                            currentIndex = 0,
                            totalCards = 0,
                            isLoading = false,
                            errorMessage = "${trainingLevel}단계 훈련소에 학습할 카드가 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "카드를 불러오는데 실패했습니다: ${e.message}"
                )
            }
        }
    }

    fun checkAnswer(userAnswer: String) {
        val currentCard = _uiState.value.currentCard ?: return
        
        // 정답 체크 (대소문자 무시, 공백 제거)
        val isCorrect = userAnswer.lowercase().trim() == currentCard.back.lowercase().trim()
        
        _uiState.value = _uiState.value.copy(
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.INCORRECT,
            correctCount = if (isCorrect) _uiState.value.correctCount + 1 else _uiState.value.correctCount,
            incorrectCount = if (!isCorrect) _uiState.value.incorrectCount + 1 else _uiState.value.incorrectCount
        )
        
        // 카드 박스 업데이트
        updateCardBox(currentCard, isCorrect)
    }

    private fun updateCardBox(card: Flashcard, isCorrect: Boolean) {
        viewModelScope.launch {
            try {
                val updatedCard = if (isCorrect) {
                    // 정답: 다음 박스로 이동 (최대 5단계)
                    card.moveToNextBox()
                } else {
                    // 오답: 1단계로 이동
                    card.moveToFirstBox()
                }
                
                repository.updateFlashcard(updatedCard)
            } catch (e: Exception) {
                // 업데이트 실패해도 학습은 계속 진행
                println("카드 업데이트 실패: ${e.message}")
            }
        }
    }

    fun nextCard() {
        currentCardIndex++
        
        if (currentCardIndex >= allCards.size) {
            // 모든 카드 완료
            _uiState.value = _uiState.value.copy(
                isCompleted = true
            )
        } else {
            // 다음 카드로 이동
            _uiState.value = _uiState.value.copy(
                currentCard = allCards[currentCardIndex],
                currentIndex = currentCardIndex + 1,
                answerState = AnswerState.WAITING,
                remainingCards = allCards.drop(currentCardIndex + 1)
            )
        }
    }

    fun resetAnswer() {
        _uiState.value = _uiState.value.copy(answerState = AnswerState.WAITING)
    }
} 