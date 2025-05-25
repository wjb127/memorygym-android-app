package com.memorygym.app.presentation.splash

import androidx.lifecycle.ViewModel
import com.memorygym.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _isUserSignedIn.value = authRepository.isUserSignedIn()
    }
} 