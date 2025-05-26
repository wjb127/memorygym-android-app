# 🔥 Firebase Firestore 설정 가이드

## 📋 **1. Firebase Console에서 Firestore 활성화**

### 1️⃣ **Firestore 데이터베이스 생성**
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. `memorygym-5b902` 프로젝트 선택
3. 왼쪽 메뉴에서 **"Firestore Database"** 클릭
4. **"데이터베이스 만들기"** 버튼 클릭
5. **"테스트 모드에서 시작"** 선택 (나중에 보안 규칙 적용)
6. 위치 선택: **"asia-northeast3 (서울)"** 권장
7. **"완료"** 클릭

### 2️⃣ **보안 규칙 설정**
1. Firestore Database > **"규칙"** 탭 클릭
2. 기존 규칙을 다음으로 교체:

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

3. **"게시"** 버튼 클릭

## 📋 **2. 인덱스 생성**

### 1️⃣ **복합 인덱스 생성**
Firestore Console > **"인덱스"** 탭에서 다음 인덱스들을 생성:

#### **flashcards 컬렉션**
```
컬렉션 ID: flashcards
필드: userId (오름차순), subjectId (오름차순), createdAt (내림차순)
```

```
컬렉션 ID: flashcards
필드: userId (오름차순), nextReview (오름차순)
```

#### **subjects 컬렉션**
```
컬렉션 ID: subjects
필드: userId (오름차순), createdAt (내림차순)
```

#### **studySessions 컬렉션**
```
컬렉션 ID: studySessions
필드: userId (오름차순), createdAt (내림차순)
```

```
컬렉션 ID: studySessions
필드: userId (오름차순), subjectId (오름차순), createdAt (내림차순)
```

### 2️⃣ **단일 필드 인덱스**
다음 필드들에 대해 단일 필드 인덱스가 자동 생성됩니다:
- `userId`
- `subjectId`
- `createdAt`
- `nextReview`

## 📋 **3. 초기 데이터 설정**

### 1️⃣ **복습 간격 데이터 추가 (5단계 시스템)**
Firestore Console > **"데이터"** 탭에서 `reviewIntervals` 컬렉션 생성 후 다음 문서들 추가:

```
문서 ID: 1
데이터: { boxNumber: 1, intervalDays: 1 }   // 1일 후

문서 ID: 2
데이터: { boxNumber: 2, intervalDays: 3 }   // 3일 후

문서 ID: 3
데이터: { boxNumber: 3, intervalDays: 7 }   // 1주일 후

문서 ID: 4
데이터: { boxNumber: 4, intervalDays: 14 }  // 2주일 후

문서 ID: 5
데이터: { boxNumber: 5, intervalDays: 30 }  // 1달 후
```

## 📋 **4. Android 앱에서 Firestore 사용**

### 1️⃣ **의존성 확인**
`app/build.gradle.kts`에 다음이 포함되어 있는지 확인:

```kotlin
implementation("com.google.firebase:firebase-firestore")
```

### 2️⃣ **Repository 사용 예시**

```kotlin
@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()
    
    init {
        observeSubjects()
    }
    
    private fun observeSubjects() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    firestoreRepository.getSubjects(user.uid).collect { subjectList ->
                        _subjects.value = subjectList
                    }
                }
            }
        }
    }
    
    fun createSubject(name: String, description: String?) {
        viewModelScope.launch {
            authRepository.currentUser.value?.let { user ->
                val subject = Subject(
                    name = name,
                    description = description,
                    userId = user.uid
                )
                firestoreRepository.createSubject(subject)
            }
        }
    }
}
```

## 📋 **5. 오프라인 지원 설정**

### 1️⃣ **Application 클래스에서 설정**
`MemoryGymApplication.kt`에 추가:

```kotlin
override fun onCreate() {
    super.onCreate()
    
    // Firestore 오프라인 지원 활성화
    FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings.toBuilder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
    }
}
```

## 📋 **6. 데이터 마이그레이션 (선택사항)**

기존 PostgreSQL 데이터가 있는 경우:

### 1️⃣ **데이터 내보내기**
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

### 2️⃣ **Firebase Admin SDK로 가져오기**
`firestore-init-data.js` 스크립트 사용

## 🚀 **완료 확인**

1. ✅ Firestore 데이터베이스 생성됨
2. ✅ 보안 규칙 설정됨
3. ✅ 필요한 인덱스 생성됨
4. ✅ 복습 간격 초기 데이터 추가됨
5. ✅ Android 앱에서 Firestore 연동 완료

이제 MemoryGym Android 앱에서 Firestore를 사용할 준비가 완료되었습니다! 🎉 