package com.memorygym.app.presentation.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.memorygym.app.presentation.navigation.Screen
import com.memorygym.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TrainingStudyScreen(
    navController: NavController,
    subjectId: String,
    subjectName: String,
    trainingLevel: Int,
    viewModel: TrainingStudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var userAnswer by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // UI 전용 상태 관리 (ViewModel의 answerState와 분리)
    var uiAnswerState by remember { mutableStateOf(AnswerState.WAITING) }
    // 정답/오답 표시용 카드 정보 저장
    var answerDisplayCard by remember { mutableStateOf<com.memorygym.app.data.model.Flashcard?>(null) }
    // UI에서 직접 관리하는 답변 횟수 (ViewModel과 독립적)
    var uiAnsweredCount by remember { mutableStateOf(1) }
    // UI에서 직접 관리하는 카드 배열
    var uiCards by remember { mutableStateOf<List<com.memorygym.app.data.model.Flashcard>>(emptyList()) }

    LaunchedEffect(subjectId, trainingLevel) {
        viewModel.loadCardsForTraining(subjectId, trainingLevel)
    }

    // ViewModel에서 카드가 로드되면 UI 카드 배열에 복사
    LaunchedEffect(uiState.allCards) {
        if (uiState.allCards.isNotEmpty() && uiCards.isEmpty()) {
            // 첫 번째 로드일 때만 카드 배열 설정
            uiCards = uiState.allCards
            println("DEBUG: UI 카드 배열 초기화 - ${uiCards.size}개 카드 로드")
        }
    }

    // ViewModel의 answerState 변경 감지는 더 이상 사용하지 않음 (UI에서 직접 관리)
    // LaunchedEffect는 제거하고 UI에서만 상태 관리

    // 현재 표시할 카드 계산 (uiAnsweredCount 기준)
    val currentDisplayCard = remember(uiAnsweredCount, uiCards) {
        if (uiCards.isNotEmpty() && uiAnsweredCount <= uiCards.size) {
            val cardIndex = uiAnsweredCount - 1 // 1-based를 0-based로 변환
            if (cardIndex >= 0 && cardIndex < uiCards.size) {
                val card = uiCards[cardIndex]
                println("DEBUG: [WAITING] 현재 문제 - ${card.front} (${uiAnsweredCount}/${uiCards.size})")
                card
            } else {
                null
            }
        } else {
            null
        }
    }

    // UI 카운터 기반 훈련 완료 체크
    LaunchedEffect(uiAnsweredCount, uiCards.size) {
        if (uiAnsweredCount > uiCards.size && uiCards.isNotEmpty()) {
            println("DEBUG: [훈련완료] 모든 문제 완료 - $uiAnsweredCount > ${uiCards.size}")
            navController.navigate(
                Screen.TrainingResult.createRoute(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    trainingLevel = trainingLevel,
                    correctCount = uiState.correctCount,
                    incorrectCount = uiState.incorrectCount
                )
            ) {
                popUpTo(Screen.TrainingCenter.createRoute(subjectId, subjectName)) {
                    inclusive = false
                }
            }
        }
    }

    // 학습 완료 시 결과 화면으로 이동
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            navController.navigate(
                Screen.TrainingResult.createRoute(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    trainingLevel = trainingLevel,
                    correctCount = uiState.correctCount,
                    incorrectCount = uiState.incorrectCount
                )
            ) {
                popUpTo(Screen.TrainingCenter.createRoute(subjectId, subjectName)) {
                    inclusive = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${trainingLevel}단계 훈련소",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "과목 필터링 적용됨",
                            fontSize = 12.sp,
                            color = TextGray.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    Text(
                        text = "$uiAnsweredCount / ${uiCards.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPink)
                }
            } else if (uiState.currentCard != null) {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 다시 선택하기 버튼
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.Start),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ButtonGray)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("←", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("다시 선택하기", color = TextGray, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 학습 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (uiAnswerState) {
                                AnswerState.CORRECT -> Color(0xFFE8F5E8)
                                AnswerState.INCORRECT -> Color(0xFFFFE8E8)
                                else -> Color.White
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(
                            width = 3.dp,
                            color = when (uiAnswerState) {
                                AnswerState.CORRECT -> Color(0xFF4CAF50)
                                AnswerState.INCORRECT -> Color(0xFFFF5252)
                                else -> Color.Transparent
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 훈련소 표시
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = LightGray)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏆", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "훈련소 $trainingLevel",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextGray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // 문제 표시
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = LightGray)
                            ) {
                                Text(
                                    text = when (uiAnswerState) {
                                        AnswerState.WAITING -> {
                                            // WAITING 상태에서는 현재 표시할 카드 사용
                                            currentDisplayCard?.front ?: ""
                                        }
                                        AnswerState.CORRECT, AnswerState.INCORRECT -> {
                                            // 정답/오답 상태에서는 정답 확인 시점의 카드 사용
                                            answerDisplayCard?.front ?: ""
                                        }
                                    },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // 정답 입력 또는 결과 표시
                            when (uiAnswerState) {
                                AnswerState.WAITING -> {
                                    OutlinedTextField(
                                        value = userAnswer,
                                        onValueChange = { 
                                            userAnswer = it
                                            // 키보드가 올라왔을 때 자동 스크롤
                                        },
                                        placeholder = { 
                                            Text(
                                                "정답을 입력하세요",
                                                color = TextGray.copy(alpha = 0.6f)
                                            ) 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentPink,
                                            unfocusedBorderColor = LightGray
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                // UI에서 직접 정답 체크 (ViewModel 호출하지 않음)
                                                val currentQuestionIndex = uiAnsweredCount - 1
                                                if (uiCards.isNotEmpty() && currentQuestionIndex >= 0 && currentQuestionIndex < uiCards.size) {
                                                    val questionCard = uiCards[currentQuestionIndex]
                                                    val isCorrect = userAnswer.lowercase().trim() == questionCard.back.lowercase().trim()
                                                    
                                                    println("DEBUG: [키보드 정답확인] ${questionCard.front} -> 입력: '$userAnswer' / 정답: '${questionCard.back}' / 결과: ${if (isCorrect) "정답" else "오답"}")
                                                    
                                                    // 먼저 정답/오답 표시용 카드 설정
                                                    answerDisplayCard = questionCard
                                                    
                                                    // UI 상태 업데이트
                                                    uiAnswerState = if (isCorrect) AnswerState.CORRECT else AnswerState.INCORRECT
                                                    uiAnsweredCount++
                                                    
                                                    // ViewModel에는 카드 박스 업데이트만 요청
                                                    viewModel.updateCardBoxOnly(questionCard, isCorrect)
                                                }
                                            }
                                        ),
                                        singleLine = true
                                    )
                                    
                                    // 키보드가 올라왔을 때 자동 스크롤
                                    LaunchedEffect(uiAnswerState) {
                                        if (uiAnswerState == AnswerState.WAITING) {
                                            delay(300) // 키보드 애니메이션 대기
                                            scrollState.animateScrollTo(scrollState.maxValue)
                                        }
                                    }
                                }
                                AnswerState.CORRECT -> {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text(
                                            text = answerDisplayCard?.back ?: "",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                                AnswerState.INCORRECT -> {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252))
                                    ) {
                                        Text(
                                            text = answerDisplayCard?.back ?: "",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // 액션 버튼 (고정 높이로 동일한 위치 보장)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp) // 고정 높이
                            ) {
                                when (uiAnswerState) {
                                    AnswerState.WAITING -> {
                                        Button(
                                            onClick = {
                                                keyboardController?.hide()
                                                // 빈 답안도 허용 (모르는 경우 바로 오답 처리)
                                                
                                                // 현재 문제의 인덱스 계산 (정답 확인 시점 기준)
                                                val currentQuestionIndex = uiAnsweredCount - 1
                                                
                                                // UI에서 직접 정답 체크
                                                if (uiCards.isNotEmpty() && currentQuestionIndex >= 0 && currentQuestionIndex < uiCards.size) {
                                                    val questionCard = uiCards[currentQuestionIndex]
                                                    val isCorrect = userAnswer.lowercase().trim() == questionCard.back.lowercase().trim()
                                                    
                                                    // 먼저 정답/오답 표시용 카드 설정 (uiAnsweredCount 증가 전에!)
                                                    answerDisplayCard = questionCard
                                                    
                                                    // 그 다음 UI 상태 업데이트
                                                    uiAnswerState = if (isCorrect) AnswerState.CORRECT else AnswerState.INCORRECT
                                                    uiAnsweredCount++
                                                    
                                                    println("DEBUG: [정답확인] ${questionCard.front} -> 입력: '$userAnswer' / 정답: '${questionCard.back}' / 결과: ${if (isCorrect) "정답" else "오답"}")
                                                    
                                                    // ViewModel에는 카드 박스 업데이트만 요청
                                                    viewModel.updateCardBoxOnly(questionCard, isCorrect)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("정답 확인", color = Color.White, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Enter ↵", color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                    // 정답 또는 오답 상태일 때 표시되는 "다음으로 넘어가기" 버튼
                                    AnswerState.CORRECT, AnswerState.INCORRECT -> {
                                        
                                        Button(
                                            onClick = {
                                                // 입력 필드 초기화
                                                userAnswer = ""
                                                // UI 상태를 WAITING으로 변경
                                                uiAnswerState = AnswerState.WAITING
                                                // 정답/오답 표시용 카드 정보 초기화
                                                answerDisplayCard = null
                                                // uiAnsweredCount는 이미 정답 확인 시 증가했으므로 여기서는 건드리지 않음
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("🧠", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (uiAnsweredCount < uiCards.size) {
                                                        // 다음 문제가 있을 때
                                                        "다음으로 넘어가기"
                                                    } else {
                                                        // 마지막 문제일 때
                                                        "학습 완료"
                                                    },
                                                    color = Color.White,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 정답/오답 메시지 (버튼 아래)
                            when (uiAnswerState) {
                                AnswerState.CORRECT -> {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎯", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "정답입니다!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                                AnswerState.INCORRECT -> {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("❌", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "틀렸습니다!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF5252)
                                        )
                                    }
                                }
                                AnswerState.WAITING -> {
                                    // 대기 상태에서는 메시지 없음
                                }
                            }
                        }
                    }
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "오류가 발생했습니다",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            fontSize = 14.sp,
                            color = TextGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("📚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${trainingLevel}단계 훈련소가 비어있습니다",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "다른 훈련소를 선택하거나\n퀴즈를 추가해보세요",
                            fontSize = 14.sp,
                            color = TextGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AccentPink)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("←", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("훈련소 선택으로 돌아가기", color = AccentPink, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            // 하단 앱 정보
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🧠", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "암기훈련소 - 매일 훈련하는 두뇌는 더 강해집니다",
                    fontSize = 12.sp,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
} 