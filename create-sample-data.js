// MemoryGym 테스트용 샘플 데이터 생성 스크립트
// 사용법: node create-sample-data.js [USER_ID]

const admin = require('firebase-admin');

// Firebase Admin SDK 초기화
admin.initializeApp({
  projectId: 'memorygym-5b902'
});

const db = admin.firestore();

// 명령행 인수에서 사용자 ID 가져오기
const userId = process.argv[2] || 'sample_user_123';

console.log(`🎯 사용자 ID: ${userId}로 샘플 데이터 생성`);

// 샘플 사용자 데이터
const sampleUser = {
  id: userId,
  email: 'sample@memorygym.com',
  displayName: '테스트 사용자',
  photoURL: '',
  createdAt: admin.firestore.Timestamp.now(),
  updatedAt: admin.firestore.Timestamp.now(),
  isPremium: false,
  premiumUntil: null,
  profile: {
    username: 'testuser',
    fullName: '테스트 사용자',
    avatarUrl: null
  }
};

// 샘플 과목 데이터
const sampleSubjects = [
  {
    name: '영어 단어',
    description: '기본 영어 단어 학습',
    userId: userId,
    cardCount: 0,
    lastStudied: null
  },
  {
    name: '한국사',
    description: '한국사 기본 개념',
    userId: userId,
    cardCount: 0,
    lastStudied: null
  },
  {
    name: '프로그래밍',
    description: 'JavaScript 기초',
    userId: userId,
    cardCount: 0,
    lastStudied: null
  }
];

// 샘플 플래시카드 데이터 (과목별)
const sampleFlashcards = {
  '영어 단어': [
    { front: 'Apple', back: '사과' },
    { front: 'Book', back: '책' },
    { front: 'Computer', back: '컴퓨터' },
    { front: 'House', back: '집' },
    { front: 'Water', back: '물' }
  ],
  '한국사': [
    { front: '고구려 건국년도', back: '기원전 37년' },
    { front: '조선 건국자', back: '이성계' },
    { front: '한글 창제자', back: '세종대왕' },
    { front: '임진왜란 시작년도', back: '1592년' }
  ],
  '프로그래밍': [
    { front: 'const란?', back: '상수 선언 키워드' },
    { front: 'function이란?', back: '함수 선언 키워드' },
    { front: 'array란?', back: '배열 자료구조' }
  ]
};

async function createSampleUser() {
  console.log('👤 샘플 사용자 생성...');
  
  try {
    await db.collection('users').doc(userId).set(sampleUser);
    console.log('✅ 사용자 생성 완료');
  } catch (error) {
    console.error('❌ 사용자 생성 실패:', error);
  }
}

async function createSampleSubjects() {
  console.log('📚 샘플 과목 생성...');
  
  const createdSubjects = [];
  
  try {
    for (const subject of sampleSubjects) {
      const docRef = db.collection('subjects').doc();
      const subjectWithId = {
        ...subject,
        id: docRef.id,
        createdAt: admin.firestore.Timestamp.now()
      };
      
      await docRef.set(subjectWithId);
      createdSubjects.push(subjectWithId);
      console.log(`📖 과목 생성: ${subject.name}`);
    }
    
    console.log('✅ 과목 생성 완료');
    return createdSubjects;
  } catch (error) {
    console.error('❌ 과목 생성 실패:', error);
    return [];
  }
}

async function createSampleFlashcards(subjects) {
  console.log('🃏 샘플 플래시카드 생성...');
  
  try {
    let totalCards = 0;
    
    for (const subject of subjects) {
      const cards = sampleFlashcards[subject.name] || [];
      
      for (const cardData of cards) {
        const docRef = db.collection('flashcards').doc();
        const card = {
          id: docRef.id,
          front: cardData.front,
          back: cardData.back,
          boxNumber: 1,
          lastReviewed: admin.firestore.Timestamp.now(),
          nextReview: admin.firestore.Timestamp.now(),
          isAdminCard: false,
          subjectId: subject.id,
          userId: userId,
          createdAt: admin.firestore.Timestamp.now(),
          difficulty: 1,
          reviewCount: 0
        };
        
        await docRef.set(card);
        totalCards++;
      }
      
      // 과목의 카드 개수 업데이트
      await db.collection('subjects').doc(subject.id).update({
        cardCount: cards.length
      });
      
      console.log(`🃏 ${subject.name}: ${cards.length}개 카드 생성`);
    }
    
    console.log(`✅ 총 ${totalCards}개 플래시카드 생성 완료`);
  } catch (error) {
    console.error('❌ 플래시카드 생성 실패:', error);
  }
}

async function createSampleStudySession(subjects) {
  console.log('📊 샘플 학습 세션 생성...');
  
  try {
    if (subjects.length > 0) {
      const docRef = db.collection('studySessions').doc();
      const session = {
        id: docRef.id,
        userId: userId,
        subjectId: subjects[0].id,
        cardsStudied: 5,
        correctAnswers: 3,
        startTime: admin.firestore.Timestamp.fromDate(new Date(Date.now() - 600000)), // 10분 전
        endTime: admin.firestore.Timestamp.now(),
        duration: 600, // 10분
        createdAt: admin.firestore.Timestamp.now()
      };
      
      await docRef.set(session);
      console.log('✅ 학습 세션 생성 완료');
    }
  } catch (error) {
    console.error('❌ 학습 세션 생성 실패:', error);
  }
}

async function createSampleFeedback() {
  console.log('💬 샘플 피드백 생성...');
  
  try {
    const docRef = db.collection('feedback').doc();
    const feedback = {
      id: docRef.id,
      content: '앱이 정말 유용해요! 간격 반복 학습이 효과적입니다.',
      email: 'sample@memorygym.com',
      userId: userId,
      createdAt: admin.firestore.Timestamp.now()
    };
    
    await docRef.set(feedback);
    console.log('✅ 피드백 생성 완료');
  } catch (error) {
    console.error('❌ 피드백 생성 실패:', error);
  }
}

async function main() {
  console.log('🚀 MemoryGym 샘플 데이터 생성 시작\n');
  
  await createSampleUser();
  const subjects = await createSampleSubjects();
  await createSampleFlashcards(subjects);
  await createSampleStudySession(subjects);
  await createSampleFeedback();
  
  console.log('\n🎉 모든 샘플 데이터 생성 완료!');
  console.log(`📱 Android 앱에서 사용자 ID "${userId}"로 로그인하여 테스트하세요.`);
  
  process.exit(0);
}

main().catch(console.error); 