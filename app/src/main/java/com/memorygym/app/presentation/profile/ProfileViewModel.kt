package com.memorygym.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorygym.app.data.model.User
import com.memorygym.app.data.repository.AuthRepository
import com.memorygym.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val userResult = firestoreRepository.getUser(currentUser.uid)
                    userResult.fold(
                        onSuccess = { user ->
                            _uiState.value = _uiState.value.copy(
                                user = user,
                                isLoading = false,
                                errorMessage = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "프로필 로드 실패: ${error.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "로그인이 필요합니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "프로필 로드 실패: ${e.message}"
                )
            }
        }
    }

    fun updateProfile(username: String?, fullName: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        username = username,
                        fullName = fullName,
                        updatedAt = com.google.firebase.Timestamp.now()
                    )
                    
                    val result = firestoreRepository.updateUser(updatedUser)
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                user = updatedUser,
                                isLoading = false,
                                errorMessage = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "프로필 업데이트 실패: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "프로필 업데이트 실패: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
} 