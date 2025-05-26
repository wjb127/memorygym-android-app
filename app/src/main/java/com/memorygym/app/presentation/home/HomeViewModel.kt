package com.memorygym.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.memorygym.app.data.model.Subject
import com.memorygym.app.data.model.Flashcard
import com.memorygym.app.data.repository.AuthRepository
import com.memorygym.app.data.repository.FirestoreRepository
import com.memorygym.app.data.service.GoogleSignInService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: FirebaseUser? = null,
    val isSignedOut: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedManageSubject: Subject? = null,
    val selectedTrainingLevel: Int = 0, // 0은 전체보기
    val flashcards: List<Flashcard> = emptyList(),
    val isLoadingFlashcards: Boolean = false,
    val searchQuery: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInService: GoogleSignInService,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val subjects: StateFlow<List<Subject>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                firestoreRepository.getSubjects(user.uid)
            } else {
                flowOf(emptyList()) // 로그인하지 않은 경우 빈 목록
            }
        }
        .catch { e ->
            _uiState.value = _uiState.value.copy(errorMessage = "과목 로드 실패: ${e.message}")
            emit(emptyList()) // 오류 발생 시 빈 목록
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isSignedOut = user == null
                )
                
                // 로그인한 사용자에게 초기 데이터 체크 및 생성
                user?.let { firebaseUser ->
                    checkAndCreateInitialData(firebaseUser.uid)
                }
            }
        }
    }

    private fun checkAndCreateInitialData(userId: String) {
        viewModelScope.launch {
            try {
                val hasInitialData = firestoreRepository.hasInitialData(userId)
                if (hasInitialData.isSuccess && !hasInitialData.getOrThrow()) {
                    val result = firestoreRepository.createInitialDataForUser(userId)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "중급 영단어 과목과 50개 퀴즈가 자동으로 생성되었습니다!"
                        )
                    }
                }
            } catch (e: Exception) {
                // 초기 데이터 생성 실패는 조용히 처리 (사용자 경험에 영향 없음)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleSignInService.signOut()
            authRepository.signOut()
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
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = null
                                // 과목 생성 성공 시 subjects StateFlow가 자동으로 업데이트될 것으로 예상
                            )
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
            } ?: run {
                 _uiState.value = _uiState.value.copy(errorMessage = "로그인이 필요합니다.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun createInitialDataForCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { currentUser ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                try {
                    val result = firestoreRepository.createInitialDataForUser(currentUser.uid)
                    
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "중급 영단어 과목과 50개 퀴즈가 생성되었습니다!"
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "초기 데이터 생성 실패: ${error.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "초기 데이터 생성 실패: ${e.message}"
                    )
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(errorMessage = "로그인이 필요합니다.")
            }
        }
    }

    fun addQuiz(subjectId: String, question: String, answer: String) {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { currentUser ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val flashcard = Flashcard(
                    front = question.trim(),
                    back = answer.trim(),
                    subjectId = subjectId,
                    userId = currentUser.uid
                )
                
                try {
                    val result = firestoreRepository.createFlashcard(flashcard)
                    
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = null,
                                successMessage = "퀴즈가 추가되었습니다"
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "퀴즈 추가 실패: ${error.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "퀴즈 추가 실패: ${e.message}"
                    )
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(errorMessage = "로그인이 필요합니다.")
            }
        }
    }

    fun setManageSubject(subject: Subject?) {
        _uiState.value = _uiState.value.copy(selectedManageSubject = subject)
        if (subject != null) {
            loadFlashcards(subject.id, _uiState.value.selectedTrainingLevel)
        } else {
            _uiState.value = _uiState.value.copy(flashcards = emptyList())
        }
    }

    fun setTrainingLevel(level: Int) {
        _uiState.value = _uiState.value.copy(selectedTrainingLevel = level)
        _uiState.value.selectedManageSubject?.let { subject ->
            loadFlashcards(subject.id, level)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        _uiState.value.selectedManageSubject?.let { subject ->
            loadFlashcards(subject.id, _uiState.value.selectedTrainingLevel)
        }
    }

    private fun loadFlashcards(subjectId: String, trainingLevel: Int) {
        viewModelScope.launch {
            authRepository.currentUser.first()?.let { currentUser ->
                _uiState.value = _uiState.value.copy(isLoadingFlashcards = true)
                
                try {
                    firestoreRepository.getFlashcardsBySubject(currentUser.uid, subjectId).collect { allFlashcards ->
                        val filteredCards = allFlashcards
                            .filter { flashcard ->
                                // 0이면 전체보기, 그 외에는 해당 단계만
                                if (trainingLevel == 0) true else flashcard.boxNumber == trainingLevel
                            }
                            .filter { flashcard ->
                                if (_uiState.value.searchQuery.isBlank()) {
                                    true
                                } else {
                                    flashcard.front.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                                    flashcard.back.contains(_uiState.value.searchQuery, ignoreCase = true)
                                }
                            }
                        
                        _uiState.value = _uiState.value.copy(
                            flashcards = filteredCards,
                            isLoadingFlashcards = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingFlashcards = false,
                        errorMessage = "퀴즈 로드 실패: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = firestoreRepository.deleteFlashcard(flashcard.id)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "퀴즈가 삭제되었습니다"
                        )
                        // 목록 새로고침
                        _uiState.value.selectedManageSubject?.let { subject ->
                            loadFlashcards(subject.id, _uiState.value.selectedTrainingLevel)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "퀴즈 삭제 실패: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "퀴즈 삭제 실패: ${e.message}"
                )
            }
        }
    }

    fun updateFlashcard(flashcard: Flashcard, newFront: String, newBack: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val updatedFlashcard = flashcard.copy(
                    front = newFront.trim(),
                    back = newBack.trim()
                )
                
                val result = firestoreRepository.updateFlashcard(updatedFlashcard)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "퀴즈가 수정되었습니다"
                        )
                        // 목록 새로고침
                        _uiState.value.selectedManageSubject?.let { subject ->
                            loadFlashcards(subject.id, _uiState.value.selectedTrainingLevel)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "퀴즈 수정 실패: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "퀴즈 수정 실패: ${e.message}"
                )
            }
        }
    }
} 