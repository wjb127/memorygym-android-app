package com.memorygym.app.presentation.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.memorygym.app.presentation.navigation.Screen
import com.memorygym.app.presentation.theme.*

data class TrainingCenter(
    val level: Int,
    val name: String,
    val description: String,
    val icon: String,
    val cardCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingCenterScreen(
    navController: NavController,
    subjectId: String,
    subjectName: String,
    viewModel: TrainingCenterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.loadTrainingCenters(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧠", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "두뇌 훈련하기",
                                fontWeight = FontWeight.Bold,
                                color = AccentPink
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTrainingCenters(subjectId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침", tint = TextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "학습할 과목 선택",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 체험 모드 안내
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightSkyBlue)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = "체험 모드",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "체험 모드: 샘플 과목으로 학습을 체험해보세요! (수정/삭제 불가)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 선택된 과목 표시
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGray),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = subjectName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            item {
                Text(
                    text = "훈련소를 선택하세요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPink)
                    }
                }
            } else {
                items(uiState.trainingCenters) { center ->
                    TrainingCenterCard(
                        trainingCenter = center,
                        onClick = {
                            navController.navigate(Screen.TrainingStudy.createRoute(subjectId, subjectName, center.level))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingCenterCard(
    trainingCenter: TrainingCenter,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trainingCenter.icon,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = trainingCenter.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = trainingCenter.description,
                        fontSize = 14.sp,
                        color = TextGray.copy(alpha = 0.7f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trainingCenter.cardCount} 퀴즈",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPink
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("→", fontSize = 20.sp, color = TextGray)
            }
        }
    }
} 