#!/bin/bash

echo "🔥 Firebase 연동을 활성화합니다..."

# 1. build.gradle.kts에서 Firebase 플러그인 활성화
echo "📝 build.gradle.kts 수정 중..."
sed -i '' 's|//     id("com.google.gms.google-services") version|    id("com.google.gms.google-services") version|g' build.gradle.kts
sed -i '' 's|//     id("com.google.firebase.crashlytics") version|    id("com.google.firebase.crashlytics") version|g' build.gradle.kts

# 2. app/build.gradle.kts에서 Firebase 플러그인 활성화
echo "📝 app/build.gradle.kts 수정 중..."
sed -i '' 's|//     id("com.google.gms.google-services")|    id("com.google.gms.google-services")|g' app/build.gradle.kts
sed -i '' 's|//     id("com.google.firebase.crashlytics")|    id("com.google.firebase.crashlytics")|g' app/build.gradle.kts

# 3. Firebase 의존성 활성화
sed -i '' 's|//     implementation(platform("com.google.firebase:firebase-bom|    implementation(platform("com.google.firebase:firebase-bom|g' app/build.gradle.kts
sed -i '' 's|//     implementation("com.google.firebase:|    implementation("com.google.firebase:|g' app/build.gradle.kts

# 4. AppModule.kt에서 Firebase 의존성 활성화
echo "📝 AppModule.kt 수정 중..."
sed -i '' 's|// import com.google.firebase|import com.google.firebase|g' app/src/main/java/com/memorygym/app/di/AppModule.kt
sed -i '' 's|// import com.memorygym.app.data.repository|import com.memorygym.app.data.repository|g' app/src/main/java/com/memorygym/app/di/AppModule.kt

# 5. 백업된 Firebase 파일들 복원
echo "📁 Firebase 관련 파일들 복원 중..."
if [ -f "app/src/main/java/com/memorygym/app/data/repository/AuthRepository.kt.bak" ]; then
    mv app/src/main/java/com/memorygym/app/data/repository/AuthRepository.kt.bak app/src/main/java/com/memorygym/app/data/repository/AuthRepository.kt
fi

if [ -f "app/src/main/java/com/memorygym/app/data/repository/AuthRepositoryImpl.kt.bak" ]; then
    mv app/src/main/java/com/memorygym/app/data/repository/AuthRepositoryImpl.kt.bak app/src/main/java/com/memorygym/app/data/repository/AuthRepositoryImpl.kt
fi

if [ -f "app/src/main/java/com/memorygym/app/data/service/GoogleSignInService.kt.bak" ]; then
    mv app/src/main/java/com/memorygym/app/data/service/GoogleSignInService.kt.bak app/src/main/java/com/memorygym/app/data/service/GoogleSignInService.kt
fi

if [ -f "app/src/main/java/com/memorygym/app/service/MemoryGymFirebaseMessagingService.kt.bak" ]; then
    mv app/src/main/java/com/memorygym/app/service/MemoryGymFirebaseMessagingService.kt.bak app/src/main/java/com/memorygym/app/service/MemoryGymFirebaseMessagingService.kt
fi

# 6. AndroidManifest.xml에서 Firebase 서비스 활성화
echo "📝 AndroidManifest.xml 수정 중..."
sed -i '' 's|<!-- Firebase Messaging Service - 임시로 주석 처리|<!-- Firebase Messaging Service|g' app/src/main/AndroidManifest.xml
sed -i '' 's|-->||g' app/src/main/AndroidManifest.xml

echo "✅ Firebase 활성화 완료!"
echo ""
echo "🔑 다음 단계:"
echo "1. Firebase Console에서 google-services.json 다운로드"
echo "2. app/ 디렉토리에 복사"
echo "3. 웹 클라이언트 ID를 app/src/main/res/values/strings.xml에 추가"
echo "4. ./gradlew clean assembleDebug 실행"
echo ""
echo "📋 필요한 정보:"
echo "- 웹 클라이언트 ID: Firebase Console → 프로젝트 설정 → 일반 → 웹 API 키"
echo "- SHA-1 키: 65:95:A9:5E:FB:63:2C:02:D5:B5:69:23:70:93:04:C4:D9:C7:4D:FA" 