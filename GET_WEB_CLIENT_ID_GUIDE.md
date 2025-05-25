# 🔑 웹 클라이언트 ID 받는 상세 가이드

## 📋 Firebase Console 방법 (권장)

### 1단계: Authentication 설정
1. Firebase Console → `memorygym-5b902` 프로젝트
2. **Authentication** → **Sign-in method**
3. **Google** 클릭 → **사용 설정** ON
4. **프로젝트 지원 이메일** 선택
5. **저장**

### 2단계: SHA-1 키 등록
1. **프로젝트 설정** (⚙️) → **일반**
2. **내 앱** → Android 앱 선택
3. **SHA 인증서 지문** → **지문 추가**
4. **SHA-1** 선택
5. 키 입력: `65:95:A9:5E:FB:63:2C:02:D5:B5:69:23:70:93:04:C4:D9:C7:4D:FA`
6. **저장**

### 3단계: 새 google-services.json 다운로드
1. **프로젝트 설정** → **일반**
2. **google-services.json 다운로드**
3. 파일을 `app/` 폴더에 복사

### 4단계: 웹 클라이언트 ID 확인
새로운 `google-services.json`에서 `oauth_client` 배열을 확인:
```json
"oauth_client": [
  {
    "client_id": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

## 🔧 Google Cloud Console 방법 (대안)

### 1단계: Google Cloud Console 접속
1. https://console.cloud.google.com/
2. `memorygym-5b902` 프로젝트 선택

### 2단계: OAuth 2.0 클라이언트 ID 생성
1. **API 및 서비스** → **사용자 인증 정보**
2. **+ 사용자 인증 정보 만들기** → **OAuth 클라이언트 ID**
3. **애플리케이션 유형**: **웹 애플리케이션**
4. **이름**: `MemoryGym Web Client`
5. **만들기**

### 3단계: 클라이언트 ID 복사
생성된 클라이언트 ID를 복사 (형식: `xxxxx.apps.googleusercontent.com`)

## ✅ 확인 방법

웹 클라이언트 ID가 올바른지 확인:
- 형식: `숫자-문자열.apps.googleusercontent.com`
- 예시: `123456789-abcdefghijklmnop.apps.googleusercontent.com`

## 🚨 주의사항

1. **Android 클라이언트 ID가 아닌 웹 클라이언트 ID**를 사용해야 함
2. **SHA-1 키 등록**이 필수
3. **Authentication에서 Google 로그인 활성화** 필수

## 📱 다음 단계

웹 클라이언트 ID를 받은 후:
1. `app/src/main/res/values/strings.xml` 수정
2. `default_web_client_id` 값 업데이트
3. 앱 빌드 및 테스트 