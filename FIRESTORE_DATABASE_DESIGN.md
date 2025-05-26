# 🔥 Firebase Firestore 데이터베이스 구조 설계

## 📊 기존 PostgreSQL → Firestore 마이그레이션

### 🔄 **데이터베이스 패러다임 변화**
- **PostgreSQL**: 관계형 데이터베이스 (RDBMS)
- **Firestore**: NoSQL 문서 데이터베이스

### 📁 **Firestore 컬렉션 구조**

## 1️⃣ **users** 컬렉션
```
users/{userId}
├── id: string (Firebase Auth UID)
├── email: string
├── displayName: string
├── photoURL: string
├── createdAt: timestamp
├── updatedAt: timestamp
├── isPremium: boolean
├── premiumUntil: timestamp | null
└── profile: {
    username: string | null
    fullName: string | null
    avatarUrl: string | null
}
```

## 2️⃣ **subjects** 컬렉션
```
subjects/{subjectId}
├── id: string (auto-generated)
├── name: string
├── description: string | null
├── userId: string (Firebase Auth UID)
├── createdAt: timestamp
├── cardCount: number (denormalized)
└── lastStudied: timestamp | null
```

## 3️⃣ **flashcards** 컬렉션
```
flashcards/{cardId}
├── id: string (auto-generated)
├── front: string
├── back: string
├── boxNumber: number (default: 1)
├── lastReviewed: timestamp
├── nextReview: timestamp
├── isAdminCard: boolean
├── subjectId: string (reference to subjects)
├── userId: string (Firebase Auth UID)
├── createdAt: timestamp
├── difficulty: number (1-5, for spaced repetition)
└── reviewCount: number
```

## 4️⃣ **studySessions** 컬렉션 (새로 추가)
```
studySessions/{sessionId}
├── id: string (auto-generated)
├── userId: string
├── subjectId: string
├── cardsStudied: number
├── correctAnswers: number
├── startTime: timestamp
├── endTime: timestamp
├── duration: number (seconds)
└── createdAt: timestamp
```

## 5️⃣ **reviewIntervals** 컬렉션 (설정용)
```
reviewIntervals/{boxNumber}
├── boxNumber: number (1-7)
└── intervalDays: number
```

## 6️⃣ **feedback** 컬렉션
```
feedback/{feedbackId}
├── id: string (auto-generated)
├── content: string
├── email: string | null
├── userId: string | null
└── createdAt: timestamp
```

## 🔗 **관계 처리 방법**

### **1. 사용자 기반 데이터 분리**
```javascript
// 사용자별 데이터 쿼리
const userSubjects = await db.collection('subjects')
  .where('userId', '==', currentUser.uid)
  .get();

const userFlashcards = await db.collection('flashcards')
  .where('userId', '==', currentUser.uid)
  .get();
```

### **2. 복합 쿼리 (과목별 플래시카드)**
```javascript
// 특정 과목의 플래시카드
const subjectCards = await db.collection('flashcards')
  .where('userId', '==', currentUser.uid)
  .where('subjectId', '==', selectedSubjectId)
  .get();
```

### **3. 서브컬렉션 활용 (대안 구조)**
```
users/{userId}/subjects/{subjectId}
users/{userId}/flashcards/{cardId}
users/{userId}/studySessions/{sessionId}
```

## 🛡️ **Firestore 보안 규칙**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 자신의 데이터만 접근 가능
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // 과목 데이터
    match /subjects/{subjectId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.userId;
    }
    
    // 플래시카드 데이터
    match /flashcards/{cardId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.userId;
    }
    
    // 학습 세션
    match /studySessions/{sessionId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.userId;
    }
    
    // 복습 간격 (모든 사용자 읽기 가능)
    match /reviewIntervals/{intervalId} {
      allow read: if request.auth != null;
      allow write: if false; // 관리자만 수정 가능
    }
    
    // 피드백
    match /feedback/{feedbackId} {
      allow create: if request.auth != null;
      allow read: if false; // 관리자만 읽기 가능
    }
  }
}
```

## 📱 **Android 앱에서 사용할 데이터 모델**

### **User.kt**
```kotlin
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isPremium: Boolean = false,
    val premiumUntil: Timestamp? = null,
    val profile: UserProfile = UserProfile()
)

data class UserProfile(
    val username: String? = null,
    val fullName: String? = null,
    val avatarUrl: String? = null
)
```

### **Subject.kt**
```kotlin
data class Subject(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val cardCount: Int = 0,
    val lastStudied: Timestamp? = null
)
```

### **Flashcard.kt**
```kotlin
data class Flashcard(
    val id: String = "",
    val front: String = "",
    val back: String = "",
    val boxNumber: Int = 1,
    val lastReviewed: Timestamp = Timestamp.now(),
    val nextReview: Timestamp = Timestamp.now(),
    val isAdminCard: Boolean = false,
    val subjectId: String = "",
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val difficulty: Int = 1,
    val reviewCount: Int = 0
)
```

## 🔄 **데이터 마이그레이션 전략**

### **1. 기존 데이터 내보내기**
```sql
-- PostgreSQL에서 JSON으로 내보내기
COPY (
  SELECT json_build_object(
    'users', (SELECT json_agg(row_to_json(u)) FROM users u),
    'subjects', (SELECT json_agg(row_to_json(s)) FROM subjects s),
    'flashcards', (SELECT json_agg(row_to_json(f)) FROM flashcards f)
  )
) TO '/path/to/export.json';
```

### **2. Firebase로 가져오기**
```javascript
// Firebase Admin SDK 사용
const admin = require('firebase-admin');
const fs = require('fs');

const data = JSON.parse(fs.readFileSync('export.json'));
const db = admin.firestore();

// 배치 작업으로 데이터 가져오기
const batch = db.batch();

data.users.forEach(user => {
  const userRef = db.collection('users').doc(user.id);
  batch.set(userRef, user);
});

await batch.commit();
```

## 📊 **성능 최적화**

### **1. 인덱스 생성**
- `flashcards`: `userId`, `subjectId`, `nextReview`
- `subjects`: `userId`, `createdAt`
- `studySessions`: `userId`, `createdAt`

### **2. 데이터 비정규화**
- `subjects.cardCount`: 플래시카드 개수 저장
- `users.profile`: 프로필 정보 임베드

### **3. 페이지네이션**
```kotlin
// 플래시카드 페이지네이션
val query = db.collection("flashcards")
    .whereEqualTo("userId", currentUser.uid)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .limit(20)
```

## 🚀 **다음 단계**

1. **Firestore 의존성 활성화**
2. **데이터 모델 클래스 생성**
3. **Repository 패턴 구현**
4. **CRUD 작업 구현**
5. **오프라인 지원 설정** 