package com.memorygym.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.memorygym.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreRepository: FirestoreRepository
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("로그인에 실패했습니다.")
            
            // 로그인 성공 후 프로필 생성 또는 업데이트
            createOrUpdateUserProfile(user)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun createOrUpdateUserProfile(firebaseUser: FirebaseUser): Result<Unit> {
        return try {
            val userId = firebaseUser.uid
            
            // 기존 사용자 프로필 확인
            val existingUserResult = firestoreRepository.getUser(userId)
            
            if (existingUserResult.isSuccess && existingUserResult.getOrNull() != null) {
                // 기존 사용자 - 마지막 로그인 시간만 업데이트
                val existingUser = existingUserResult.getOrNull()!!
                val updatedUser = existingUser.copy(
                    updatedAt = Timestamp.now(),
                    // 구글 정보가 변경되었을 수 있으므로 업데이트
                    fullName = firebaseUser.displayName,
                    email = firebaseUser.email ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                firestoreRepository.updateUser(updatedUser)
            } else {
                // 새로운 사용자 - 프로필 생성
                val newUser = User(
                    id = userId,
                    username = null, // 나중에 사용자가 설정 가능
                    fullName = firebaseUser.displayName,
                    email = firebaseUser.email ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    isPremium = false,
                    premiumUntil = null
                )
                firestoreRepository.createUser(newUser)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 