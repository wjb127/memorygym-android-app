package com.memorygym.app.presentation.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.memorygym.app.data.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardListScreen(
    navController: NavController,
    subjectId: String,
    subjectName: String,
    viewModel: FlashcardListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        viewModel.loadFlashcards(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subjectName,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "카드 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 학습 버튼 추가
            if (uiState.flashcards.isNotEmpty()) {
                Button(
                    onClick = { 
                        navController.navigate("study/$subjectId/$subjectName")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("학습 시작")
                }
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.flashcards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "아직 플래시카드가 없습니다",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ 버튼을 눌러 첫 카드를 추가해보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.flashcards) { flashcard ->
                        FlashcardCard(
                            flashcard = flashcard,
                            onClick = {
                                // TODO: 카드 상세보기 또는 학습 모드로 이동
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFlashcardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { front, back ->
                viewModel.createFlashcard(subjectId, front, back)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "앞면",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = flashcard.front,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "뒷면",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = flashcard.back,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "박스 ${flashcard.boxNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "다음 복습: ${formatNextReview(flashcard.nextReview)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddFlashcardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("새 플래시카드 추가")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("앞면 (질문)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("뒷면 (답)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (front.isNotBlank() && back.isNotBlank()) {
                        onConfirm(front.trim(), back.trim())
                    }
                },
                enabled = front.isNotBlank() && back.isNotBlank()
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatNextReview(timestamp: com.google.firebase.Timestamp?): String {
    return if (timestamp != null) {
        val date = timestamp.toDate()
        val now = java.util.Date()
        val diffInDays = ((date.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
        when {
            diffInDays < 0 -> "복습 필요"
            diffInDays == 0 -> "오늘"
            diffInDays == 1 -> "내일"
            else -> "${diffInDays}일 후"
        }
    } else {
        "미정"
    }
} 