# Firebase 서비스 설정 가이드

## 🔐 1. Authentication 설정

### Firebase Console에서:
1. **Authentication** → **Sign-in method** 클릭
2. **Google** 로그인 제공업체 활성화
3. **프로젝트 지원 이메일** 설정
4. **저장** 클릭

### 중요한 정보 복사:
- **웹 클라이언트 ID**: `프로젝트 설정` → `일반` → `웹 API 키` 복사
- 이 값을 나중에 `strings.xml`에 추가해야 함

## 🗄️ 2. Firestore Database 설정

### Firebase Console에서:
1. **Firestore Database** 클릭
2. **데이터베이스 만들기** 클릭
3. **보안 규칙 모드 선택**:
   - 개발 중: **테스트 모드에서 시작** (30일 후 만료)
   - 프로덕션: **잠금 모드에서 시작** (나중에 규칙 설정)
4. **위치 선택**: `asia-northeast3 (서울)` 권장
5. **완료** 클릭

### 보안 규칙 예시 (나중에 설정):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 자신의 데이터만 접근 가능
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // 플래시카드 데이터
    match /flashcards/{cardId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    
    // 학습 기록
    match /study_sessions/{sessionId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

## 📁 3. Cloud Storage 설정

### Firebase Console에서:
1. **Storage** 클릭
2. **시작하기** 클릭
3. **보안 규칙 모드 선택**: 테스트 모드
4. **위치 선택**: `asia-northeast3 (서울)`
5. **완료** 클릭

### 보안 규칙 예시:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // 사용자별 이미지 업로드
    match /users/{userId}/images/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // 플래시카드 이미지
    match /flashcards/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## ⚡ 4. Cloud Functions 설정 (선택사항)

### Firebase Console에서:
1. **Functions** 클릭
2. **시작하기** 클릭
3. **요금제 업그레이드** (Blaze 요금제 필요)

### 주요 용도:
- 복습 알림 스케줄링
- 사용자 통계 계산
- 데이터 정리 작업

## 📱 5. Cloud Messaging 설정

### Firebase Console에서:
1. **Cloud Messaging** 클릭
2. 자동으로 활성화됨
3. **서버 키** 복사 (푸시 알림 발송용)

## 📊 6. Analytics 설정 (선택사항)

### Firebase Console에서:
1. **Analytics** 클릭
2. **시작하기** 클릭
3. **Google Analytics 계정** 연결

## 🛡️ 7. Crashlytics 설정

### Firebase Console에서:
1. **Crashlytics** 클릭
2. **Crashlytics 설정** 클릭
3. 자동으로 활성화됨

## ✅ 설정 완료 체크리스트

- [ ] Authentication - Google 로그인 활성화
- [ ] Firestore Database 생성
- [ ] Cloud Storage 설정
- [ ] Cloud Messaging 활성화
- [ ] Crashlytics 활성화
- [ ] google-services.json 다운로드
- [ ] 웹 클라이언트 ID 복사
- [ ] SHA-1 키 등록 확인

## 🔑 중요한 키 정보

### 복사해야 할 정보들:
1. **웹 클라이언트 ID**: `프로젝트 설정` → `일반` → `웹 API 키`
2. **프로젝트 ID**: `프로젝트 설정` → `일반` → `프로젝트 ID`
3. **API 키**: `프로젝트 설정` → `일반` → `웹 API 키`

이 정보들을 Android 앱의 `strings.xml`에 추가해야 합니다. 