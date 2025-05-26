package com.memorygym.app.presentation.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.memorygym.app.presentation.navigation.Screen
import com.memorygym.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingResultScreen(
    navController: NavController,
    trainingLevel: Int,
    correctCount: Int,
    incorrectCount: Int,
    subjectId: String,
    subjectName: String
) {
    val totalCount = correctCount + incorrectCount
    val accuracy = if (totalCount > 0) (correctCount * 100) / totalCount else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 완료 헤더
            Text(
                text = "🏆 ${trainingLevel}단계 훈련소 완료!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 체험 모드 안내
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightSkyBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = "체험 모드",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "체험 모드로 훈련하고 계시네요!\n로그인하시면 학습 진도가 저장되고, 나만의 카드를\n추가할 수 있습니다.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 트레이닝 결과
            Text(
                text = "트레이닝 결과:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 결과 카드들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 정확히 기억
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = correctCount.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "정확히 기억",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // 더 연습 필요
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8E8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = incorrectCount.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        Text(
                            text = "더 연습 필요",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 정확도
            Text(
                text = "정확도",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 정확도 표시
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${accuracy}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 진행바
                    LinearProgressIndicator(
                        progress = accuracy / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (accuracy >= 80) Color(0xFF4CAF50) 
                               else if (accuracy >= 60) Color(0xFFFF9800)
                               else Color(0xFFFF5252),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 안내 메시지
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✨", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "정답한 카드는 다음 단계로, 틀린 카드는 1단계로 이동했습니다.\n(체험 모드에서는 실제 저장되지 않습니다)",
                        fontSize = 14.sp,
                        color = TextGray,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 액션 버튼들
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 다시 도전하기
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.TrainingStudy.createRoute(subjectId, subjectName, trainingLevel)
                        ) {
                            popUpTo(Screen.TrainingCenter.createRoute(subjectId, subjectName)) {
                                inclusive = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔄", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "다시 도전하기",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // 다른 세트 선택하기
                OutlinedButton(
                    onClick = {
                        navController.popBackStack(
                            Screen.TrainingCenter.createRoute(subjectId, subjectName),
                            inclusive = false
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ButtonGray),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔄", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "다른 세트 선택하기",
                            color = TextGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 하단 앱 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
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