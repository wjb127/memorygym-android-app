package com.memorygym.app.presentation.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.memorygym.app.data.model.Feedback
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun submitFeedback(content: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isSuccess = false,
                message = null
            )

            try {
                val feedback = Feedback(
                    id = "", // Firestore에서 자동 생성
                    content = content,
                    email = email.ifBlank { null },
                    createdAt = Timestamp.now()
                )

                repository.createFeedback(feedback)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    message = "피드백이 성공적으로 전송되었습니다. 감사합니다!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    message = "피드백 전송에 실패했습니다: ${e.message}"
                )
            }
        }
    }
} 