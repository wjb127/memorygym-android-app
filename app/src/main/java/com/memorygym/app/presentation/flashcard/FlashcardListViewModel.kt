package com.memorygym.app.presentation.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.memorygym.app.data.model.Flashcard
import com.memorygym.app.data.repository.AuthRepository
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardListUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FlashcardListViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardListUiState())
    val uiState: StateFlow<FlashcardListUiState> = _uiState.asStateFlow()

    fun loadFlashcards(subjectId: String) {
        viewModelScope.launch {
            try {
                authRepository.currentUser.collect { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        
                        try {
                            firestoreRepository.getFlashcardsBySubject(user.uid, subjectId).collect { flashcards ->
                                println("DEBUG: Received ${flashcards.size} flashcards for subject $subjectId")
                                _uiState.value = _uiState.value.copy(
                                    flashcards = flashcards,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                flashcards = emptyList(),
                                isLoading = false,
                                errorMessage = "플래시카드 로딩 실패: ${e.message}"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            flashcards = emptyList(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    flashcards = emptyList(),
                    isLoading = false,
                    errorMessage = "인증 오류: ${e.message}"
                )
            }
        }
    }

    fun createFlashcard(subjectId: String, front: String, back: String) {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { currentUser ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val flashcard = Flashcard(
                    front = front,
                    back = back,
                    boxNumber = 1, // 새 카드는 박스 1부터 시작
                    lastReviewed = Timestamp.now(),
                    nextReview = Timestamp.now(), // 즉시 복습 가능
                    isAdminCard = false,
                    subjectId = subjectId,
                    userId = currentUser.uid,
                    createdAt = Timestamp.now()
                )
                
                try {
                    val result = firestoreRepository.createFlashcard(flashcard)
                    
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            println("DEBUG: Flashcard created successfully")
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "플래시카드 생성 실패: ${error.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "플래시카드 생성 실패: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
} 