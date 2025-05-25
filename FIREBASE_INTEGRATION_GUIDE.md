# 🔥 Firebase 연동 완료 가이드

## 📋 Firebase Console 설정 완료 후 수행할 단계들

### 1️⃣ **google-services.json 파일 교체**

Firebase Console에서 다운로드한 `google-services.json` 파일을 다음 위치에 복사:

```bash
# 기존 임시 파일 백업
cp app/google-services.json app/google-services.json.backup

# 새로운 파일 복사 (다운로드 폴더에서)
cp ~/Downloads/google-services.json app/google-services.json
```

### 2️⃣ **웹 클라이언트 ID 업데이트**

Firebase Console → 프로젝트 설정 → 일반 → 웹 API 키를 복사한 후:

```bash
# strings.xml 수정
# 현재: 123456789-zyxwvutsrqponmlkjihgfedcba.apps.googleusercontent.com
# 실제 값으로 교체
```

### 3️⃣ **Firebase 활성화**

```bash
# Firebase 연동 활성화 스크립트 실행
./enable_firebase.sh
```

### 4️⃣ **빌드 및 테스트**

```bash
# 프로젝트 클린 빌드
./gradlew clean

# 디버그 빌드
./gradlew assembleDebug

# 앱 설치 및 실행
./gradlew installDebug
```

### 5️⃣ **Google 로그인 테스트**

앱 실행 후:
1. 로그인 화면에서 "Google로 로그인" 버튼 클릭
2. Google 계정 선택
3. 권한 승인
4. 홈 화면으로 이동 확인

## 🔍 문제 해결

### 로그 확인
```bash
# 앱 로그 실시간 확인
adb logcat -s MemoryGym

# Firebase 관련 로그 확인
adb logcat -s FirebaseAuth
```

### 일반적인 오류들

#### 1. **API 키 오류**
```
java.lang.IllegalArgumentException: Please set a valid API key
```
**해결방법**: `google-services.json` 파일이 올바른지 확인

#### 2. **SHA-1 키 오류**
```
Google Sign-In failed: 12500
```
**해결방법**: Firebase Console에서 SHA-1 키 등록 확인

#### 3. **웹 클라이언트 ID 오류**
```
GoogleSignInStatusCodes.SIGN_IN_FAILED
```
**해결방법**: `strings.xml`의 `default_web_client_id` 값 확인

## 📊 Firebase 서비스별 테스트 방법

### 🔐 Authentication
- Google 로그인/로그아웃
- 사용자 정보 표시
- 인증 상태 유지

### 🗄️ Firestore
- 사용자 프로필 저장/읽기
- 플래시카드 데이터 CRUD
- 실시간 동기화

### 📁 Storage
- 프로필 이미지 업로드
- 플래시카드 이미지 저장
- 파일 다운로드

### 📱 Messaging
- 푸시 알림 수신
- 토큰 등록
- 백그라운드 알림

## 🚀 다음 단계

Firebase 연동이 완료되면:

1. **플래시카드 기능 구현**
2. **복습 스케줄링 시스템**
3. **오프라인 동기화**
4. **푸시 알림 설정**
5. **사용자 통계 대시보드**

## 📞 지원

문제가 발생하면:
1. 로그 확인
2. Firebase Console 설정 재검토
3. SHA-1 키 재생성 및 등록
4. 앱 재빌드 및 재설치 