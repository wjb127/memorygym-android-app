# MemoryGym Android App

효율적인 학습을 위한 스마트 플래시카드 Android 앱

## 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose
- **아키텍처**: MVVM + Clean Architecture
- **의존성 주입**: Hilt
- **백엔드**: Firebase
  - Authentication (Google 로그인)
  - Firestore Database
  - Cloud Storage
  - Cloud Functions
  - Cloud Messaging
  - Crashlytics
- **로컬 데이터베이스**: Room
- **네트워킹**: Retrofit + OkHttp
- **이미지 로딩**: Coil
- **비동기 처리**: Coroutines + Flow

## 주요 기능

- ✅ Google OAuth 소셜 로그인
- 🔄 사용자 프로필 관리
- 📚 카드 기반 학습 시스템 (플래시카드)
- 📂 과목별 카드 분류
- ⏰ 복습 스케줄링 (간격 반복 학습)
- 💳 프리미엄 결제 시스템
- 📊 학습 통계 및 진도 추적
- 📱 푸시 알림
- 🔄 오프라인 지원

## 프로젝트 구조

```
app/
├── src/main/java/com/memorygym/app/
│   ├── data/                    # 데이터 레이어
│   │   ├── repository/          # Repository 구현체
│   │   └── service/             # 외부 서비스 (Google 로그인 등)
│   ├── di/                      # 의존성 주입 모듈
│   ├── presentation/            # UI 레이어
│   │   ├── auth/                # 인증 화면
│   │   ├── home/                # 홈 화면
│   │   ├── splash/              # 스플래시 화면
│   │   ├── navigation/          # 네비게이션
│   │   └── theme/               # 테마 및 스타일
│   └── service/                 # 백그라운드 서비스
└── src/main/res/                # 리소스 파일
```

## 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/memorygym-android.git
cd memorygym-android
```

### 2. Firebase 설정
[Firebase 설정 가이드](FIREBASE_SETUP.md)를 참고하여 Firebase 프로젝트를 설정하세요.

### 3. 의존성 설치
```bash
./gradlew build
```

### 4. 앱 실행
Android Studio에서 프로젝트를 열고 실행하거나:
```bash
./gradlew installDebug
```

## 개발 환경

- **Android Studio**: Arctic Fox 이상
- **Kotlin**: 1.9.10
- **Gradle**: 8.2.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## 빌드 변형

- **Debug**: 개발 및 테스트용
- **Release**: 프로덕션 배포용

## 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### UI 테스트 실행
```bash
./gradlew connectedAndroidTest
```

## 배포

### 1. 릴리즈 빌드 생성
```bash
./gradlew assembleRelease
```

### 2. Google Play Console 업로드
생성된 APK 또는 AAB 파일을 Google Play Console에 업로드

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참고하세요.

## 연락처

- 개발자: [Your Name]
- 이메일: your.email@example.com
- 프로젝트 링크: https://github.com/your-username/memorygym-android

## 마이그레이션 진행 상황

- [x] 프로젝트 초기 설정
- [x] Firebase 연동 구조
- [x] Google 로그인 구현
- [x] 기본 MVVM 아키텍처
- [ ] 플래시카드 기능
- [ ] 학습 시스템
- [ ] 결제 시스템
- [ ] 통계 기능
- [ ] 데이터 마이그레이션 