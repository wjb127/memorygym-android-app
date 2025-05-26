package com.memorygym.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.memorygym.app.data.repository.AuthRepository
import com.memorygym.app.data.repository.AuthRepositoryImpl
import com.memorygym.app.data.repository.FirestoreRepository
import com.memorygym.app.data.repository.FirestoreRepositoryImpl
import com.memorygym.app.data.service.GoogleSignInService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Firebase 관련 의존성들
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestoreRepository: FirestoreRepository
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestoreRepository)

    @Provides
    @Singleton
    fun provideFirestoreRepository(
        firestore: FirebaseFirestore
    ): FirestoreRepository = FirestoreRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideGoogleSignInService(
        @ApplicationContext context: Context
    ): GoogleSignInService = GoogleSignInService(context)
} 