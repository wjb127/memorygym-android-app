package com.memorygym.app.presentation.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorygym.app.data.model.Subject
import com.memorygym.app.data.repository.AuthRepository
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectListUiState(
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SubjectListViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectListUiState())
    val uiState: StateFlow<SubjectListUiState> = _uiState.asStateFlow()

    init {
        observeSubjects()
    }

    private fun observeSubjects() {
        viewModelScope.launch {
            try {
                authRepository.currentUser.collect { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        
                        try {
                            firestoreRepository.getSubjects(user.uid).collect { subjects ->
                                println("DEBUG: Received ${subjects.size} subjects for user ${user.uid}")
                                subjects.forEach { subject ->
                                    println("DEBUG: Subject - id: ${subject.id}, name: ${subject.name}, userId: ${subject.userId}")
                                }
                                _uiState.value = _uiState.value.copy(
                                    subjects = subjects,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                subjects = emptyList(),
                                isLoading = false,
                                errorMessage = "과목 로딩 실패: ${e.message}"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            subjects = emptyList(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    subjects = emptyList(),
                    isLoading = false,
                    errorMessage = "인증 오류: ${e.message}"
                )
            }
        }
    }

    fun createSubject(name: String, description: String?) {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { currentUser ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val subject = Subject(
                    name = name,
                    description = description,
                    userId = currentUser.uid
                )
                
                try {
                    val result = firestoreRepository.createSubject(subject)
                    
                    result.fold(
                        onSuccess = {
                            // 성공 시 자동으로 observeSubjects에서 업데이트됨
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "과목 생성 실패: ${error.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "과목 생성 실패: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
} 