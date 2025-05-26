// MemoryGym Firestore 초기 데이터 설정 스크립트
// 사용법: npm run setup

const admin = require('firebase-admin');

// Firebase Admin SDK 초기화 (Application Default Credentials 사용)
admin.initializeApp({
  projectId: 'memorygym-5b902'
});

const db = admin.firestore();

// 복습 간격 초기 데이터 (5단계 시스템)
const reviewIntervals = [
  { boxNumber: 1, intervalDays: 1 },   // 1일 후
  { boxNumber: 2, intervalDays: 3 },   // 3일 후
  { boxNumber: 3, intervalDays: 7 },   // 1주일 후
  { boxNumber: 4, intervalDays: 14 },  // 2주일 후
  { boxNumber: 5, intervalDays: 30 }   // 1달 후
];

async function setupReviewIntervals() {
  console.log('🔥 복습 간격 데이터 설정 시작...');
  
  try {
    const batch = db.batch();
    
    reviewIntervals.forEach(interval => {
      const docRef = db.collection('reviewIntervals').doc(interval.boxNumber.toString());
      batch.set(docRef, interval);
      console.log(`📝 Box ${interval.boxNumber}: ${interval.intervalDays}일 간격 추가`);
    });
    
    await batch.commit();
    console.log('✅ 복습 간격 데이터 설정 완료!');
    
    // 데이터 확인
    const snapshot = await db.collection('reviewIntervals').orderBy('boxNumber').get();
    console.log('\n📊 생성된 데이터:');
    snapshot.forEach(doc => {
      const data = doc.data();
      console.log(`   Box ${data.boxNumber}: ${data.intervalDays}일`);
    });
    
  } catch (error) {
    console.error('❌ 오류 발생:', error);
  }
}

async function main() {
  console.log('🚀 MemoryGym Firestore 초기 설정 시작\n');
  
  await setupReviewIntervals();
  
  console.log('\n🎉 모든 설정이 완료되었습니다!');
  console.log('이제 Android 앱에서 Firestore를 사용할 수 있습니다.');
  
  process.exit(0);
}

main().catch(console.error); 