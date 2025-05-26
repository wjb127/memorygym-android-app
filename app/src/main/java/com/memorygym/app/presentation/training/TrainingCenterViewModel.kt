package com.memorygym.app.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingCenterUiState(
    val trainingCenters: List<TrainingCenter> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TrainingCenterViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingCenterUiState())
    val uiState: StateFlow<TrainingCenterUiState> = _uiState.asStateFlow()

    fun loadTrainingCenters(subjectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val userId = repository.getCurrentUserId() ?: return@launch
                
                repository.getFlashcardsBySubject(userId, subjectId).collect { flashcards ->
                    // 각 박스별 카드 개수 계산
                    val cardCountByBox = flashcards.groupBy { it.boxNumber }
                        .mapValues { it.value.size }
                    
                    val trainingCenters = listOf(
                        TrainingCenter(
                            level = 1,
                            name = "1단계 훈련소",
                            description = "매일 퀴즈",
                            icon = "🏆",
                            cardCount = cardCountByBox[1] ?: 0
                        ),
                        TrainingCenter(
                            level = 2,
                            name = "2단계 훈련소",
                            description = "3일마다 퀴즈",
                            icon = "🥇",
                            cardCount = cardCountByBox[2] ?: 0
                        ),
                        TrainingCenter(
                            level = 3,
                            name = "3단계 훈련소",
                            description = "일주일마다 퀴즈",
                            icon = "🧠",
                            cardCount = cardCountByBox[3] ?: 0
                        ),
                        TrainingCenter(
                            level = 4,
                            name = "4단계 훈련소",
                            description = "2주마다 퀴즈",
                            icon = "🏆",
                            cardCount = cardCountByBox[4] ?: 0
                        ),
                        TrainingCenter(
                            level = 5,
                            name = "5단계 훈련소",
                            description = "한달마다 퀴즈",
                            icon = "🎯",
                            cardCount = cardCountByBox[5] ?: 0
                        )
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        trainingCenters = trainingCenters,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "훈련소 정보를 불러오는데 실패했습니다: ${e.message}"
                )
            }
        }
    }
} 