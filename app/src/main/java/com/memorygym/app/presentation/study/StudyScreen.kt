package com.memorygym.app.presentation.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.memorygym.app.data.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    navController: NavController,
    subjectId: String,
    subjectName: String,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAnswer by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        viewModel.loadFlashcardsForStudy(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$subjectName 학습",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.loadFlashcardsForStudy(subjectId)
                        showAnswer = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.currentCard == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 모든 카드를 완료했습니다!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "오늘 학습할 카드가 없습니다.\n내일 다시 확인해보세요!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("돌아가기")
                        }
                    }
                }
            } else {
                StudyCard(
                    flashcard = uiState.currentCard!!,
                    showAnswer = showAnswer,
                    onShowAnswer = { showAnswer = true },
                    onCorrect = { 
                        viewModel.markAsCorrect(uiState.currentCard!!)
                        showAnswer = false
                    },
                    onIncorrect = { 
                        viewModel.markAsIncorrect(uiState.currentCard!!)
                        showAnswer = false
                    },
                    progress = uiState.progress,
                    totalCards = uiState.totalCards
                )
            }
        }
    }
}

@Composable
fun StudyCard(
    flashcard: Flashcard,
    showAnswer: Boolean,
    onShowAnswer: () -> Unit,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    progress: Int,
    totalCards: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 진행률 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "진행률: $progress / $totalCards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "박스 ${flashcard.boxNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = if (totalCards > 0) progress.toFloat() / totalCards else 0f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 플래시카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "질문",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = flashcard.front,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                
                if (showAnswer) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Divider()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "답",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = flashcard.back,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 버튼들
        if (!showAnswer) {
            Button(
                onClick = onShowAnswer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("답 보기")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onIncorrect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("틀렸음")
                }
                
                Button(
                    onClick = onCorrect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("맞았음")
                }
            }
        }
    }
} 