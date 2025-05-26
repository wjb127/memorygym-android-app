package com.memorygym.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorygym.app.data.model.StudySession
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val totalSessions: Int = 0,
    val totalCardsStudied: Int = 0,
    val averageAccuracy: Float = 0f,
    val recentSessions: List<StudySession> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userId = repository.getCurrentUserId()
                if (userId != null) {
                    val sessions = repository.getStudySessionsByUser(userId)
                    
                    val totalSessions = sessions.size
                    val totalCards = sessions.sumOf { it.totalCards }
                    val totalCorrect = sessions.sumOf { it.correctAnswers }
                    val averageAccuracy = if (totalCards > 0) {
                        totalCorrect.toFloat() / totalCards
                    } else 0f

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalSessions = totalSessions,
                        totalCardsStudied = totalCards,
                        averageAccuracy = averageAccuracy,
                        recentSessions = sessions.take(10) // 최근 10개 세션만
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "사용자 정보를 찾을 수 없습니다"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "통계를 불러오는데 실패했습니다: ${e.message}"
                )
            }
        }
    }
} 