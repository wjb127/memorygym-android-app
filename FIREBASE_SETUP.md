# Firebase 설정 가이드

## 1. Firebase 프로젝트 생성

1. [Firebase Console](https://console.firebase.google.com/)에 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름: `MemoryGym`
4. Google Analytics 활성화 (선택사항)

## 2. Android 앱 추가

1. Firebase 프로젝트에서 "Android 앱 추가" 클릭
2. Android 패키지 이름: `com.memorygym.app`
3. 앱 닉네임: `MemoryGym Android`
4. SHA-1 인증서 지문 추가 (Google 로그인용)

### SHA-1 키 생성 방법

#### 디버그 키 (개발용)
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### 릴리즈 키 (배포용)
```bash
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

## 3. google-services.json 다운로드

1. Firebase Console에서 `google-services.json` 파일 다운로드
2. 파일을 `app/` 디렉토리에 복사

## 4. Firebase 서비스 활성화

### Authentication
1. Firebase Console → Authentication → Sign-in method
2. Google 로그인 활성화
3. 웹 클라이언트 ID 복사하여 `strings.xml`의 `default_web_client_id`에 설정

### Firestore Database
1. Firebase Console → Firestore Database
2. "데이터베이스 만들기" 클릭
3. 보안 규칙 설정 (테스트 모드로 시작)

### Cloud Storage
1. Firebase Console → Storage
2. "시작하기" 클릭
3. 보안 규칙 설정

### Cloud Functions
1. Firebase Console → Functions
2. "시작하기" 클릭
3. Node.js 환경 설정

### Cloud Messaging
1. Firebase Console → Cloud Messaging
2. 자동으로 활성화됨

### Crashlytics
1. Firebase Console → Crashlytics
2. "Crashlytics 설정" 클릭

## 5. 보안 규칙 설정

### Firestore 규칙 예시
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 자신의 데이터만 읽기/쓰기 가능
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // 카드 데이터
    match /cards/{cardId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

### Storage 규칙 예시
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 6. 환경 변수 설정

`app/src/main/res/values/strings.xml`에서 다음 값들을 실제 값으로 변경:

```xml
<string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
```

## 7. 프로젝트 빌드 및 테스트

```bash
./gradlew assembleDebug
```

## 8. Google Play Console 설정 (배포 시)

1. Google Play Console에서 앱 등록
2. SHA-1 키를 Firebase에 추가
3. 앱 서명 키 설정

## 주의사항

- `google-services.json` 파일은 버전 관리에 포함하지 마세요
- 프로덕션 환경에서는 보안 규칙을 엄격하게 설정하세요
- API 키와 민감한 정보는 환경 변수로 관리하세요 