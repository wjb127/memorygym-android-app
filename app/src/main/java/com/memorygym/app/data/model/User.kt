package com.memorygym.app.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "", // Firebase Auth UID (uuid)
    val username: String? = null, // 사용자명 (선택사항)
    val fullName: String? = null, // 구글에서 가져온 전체 이름
    val email: String = "", // 구글 이메일
    val avatarUrl: String? = null, // 구글 프로필 사진 URL
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isPremium: Boolean = false,
    val premiumUntil: Timestamp? = null
) 