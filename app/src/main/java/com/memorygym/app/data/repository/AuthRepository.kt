package com.memorygym.app.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    fun isUserSignedIn(): Boolean
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun signOut()
} 