// Firebase Admin SDK를 사용한 Firestore 초기 데이터 설정
// 사용법: node firestore-init-data.js

const admin = require('firebase-admin');

// Firebase Admin SDK 초기화 (서비스 계정 키 필요)
// const serviceAccount = require('./path/to/serviceAccountKey.json');
// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount)
// });

const db = admin.firestore();

// 복습 간격 초기 데이터
const reviewIntervals = [
  { boxNumber: 1, intervalDays: 1 },
  { boxNumber: 2, intervalDays: 3 },
  { boxNumber: 3, intervalDays: 7 },
  { boxNumber: 4, intervalDays: 14 },
  { boxNumber: 5, intervalDays: 30 },
  { boxNumber: 6, intervalDays: 90 },
  { boxNumber: 7, intervalDays: 180 }
];

async function initializeReviewIntervals() {
  console.log('복습 간격 데이터 초기화 시작...');
  
  const batch = db.batch();
  
  reviewIntervals.forEach(interval => {
    const docRef = db.collection('reviewIntervals').doc(interval.boxNumber.toString());
    batch.set(docRef, interval);
  });
  
  try {
    await batch.commit();
    console.log('복습 간격 데이터 초기화 완료!');
  } catch (error) {
    console.error('복습 간격 데이터 초기화 실패:', error);
  }
}

// 샘플 과목 데이터 (테스트용)
const sampleSubjects = [
  {
    name: '영어 단어',
    description: '기본 영어 단어 학습',
    userId: 'SAMPLE_USER_ID', // 실제 사용자 ID로 교체 필요
    cardCount: 0,
    lastStudied: null
  },
  {
    name: '한국사',
    description: '한국사 기본 개념',
    userId: 'SAMPLE_USER_ID', // 실제 사용자 ID로 교체 필요
    cardCount: 0,
    lastStudied: null
  }
];

// 샘플 플래시카드 데이터 (테스트용)
const sampleFlashcards = [
  {
    front: 'Apple',
    back: '사과',
    boxNumber: 1,
    isAdminCard: true,
    subjectId: 'SAMPLE_SUBJECT_ID', // 실제 과목 ID로 교체 필요
    userId: 'SAMPLE_USER_ID', // 실제 사용자 ID로 교체 필요
    difficulty: 1,
    reviewCount: 0
  },
  {
    front: 'Book',
    back: '책',
    boxNumber: 1,
    isAdminCard: true,
    subjectId: 'SAMPLE_SUBJECT_ID', // 실제 과목 ID로 교체 필요
    userId: 'SAMPLE_USER_ID', // 실제 사용자 ID로 교체 필요
    difficulty: 1,
    reviewCount: 0
  }
];

async function initializeSampleData() {
  console.log('샘플 데이터 초기화 시작...');
  
  try {
    // 과목 생성
    const subjectPromises = sampleSubjects.map(async (subject) => {
      const docRef = db.collection('subjects').doc();
      const subjectWithId = { ...subject, id: docRef.id, createdAt: admin.firestore.Timestamp.now() };
      await docRef.set(subjectWithId);
      return { id: docRef.id, ...subjectWithId };
    });
    
    const createdSubjects = await Promise.all(subjectPromises);
    console.log('샘플 과목 생성 완료:', createdSubjects.length);
    
    // 플래시카드 생성 (첫 번째 과목에 추가)
    if (createdSubjects.length > 0) {
      const firstSubjectId = createdSubjects[0].id;
      
      const cardPromises = sampleFlashcards.map(async (card) => {
        const docRef = db.collection('flashcards').doc();
        const cardWithId = {
          ...card,
          id: docRef.id,
          subjectId: firstSubjectId,
          createdAt: admin.firestore.Timestamp.now(),
          lastReviewed: admin.firestore.Timestamp.now(),
          nextReview: admin.firestore.Timestamp.now()
        };
        await docRef.set(cardWithId);
        return cardWithId;
      });
      
      const createdCards = await Promise.all(cardPromises);
      console.log('샘플 플래시카드 생성 완료:', createdCards.length);
      
      // 과목의 카드 개수 업데이트
      await db.collection('subjects').doc(firstSubjectId).update({
        cardCount: createdCards.length
      });
    }
    
    console.log('샘플 데이터 초기화 완료!');
  } catch (error) {
    console.error('샘플 데이터 초기화 실패:', error);
  }
}

// 실행
async function main() {
  await initializeReviewIntervals();
  // await initializeSampleData(); // 필요시 주석 해제
}

// main().catch(console.error); 