package com.memorygym.app.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.memorygym.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // 에러 메시지 표시
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 스낵바나 토스트로 에러 메시지 표시 가능
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "프로필",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "프로필 수정")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 프로필 사진
                val currentUser = uiState.user
                if (currentUser?.avatarUrl != null) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = "프로필 사진",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "기본 프로필",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 사용자 정보 카드들
                if (currentUser != null) {
                    ProfileInfoCard(
                        title = "이름",
                        value = currentUser.fullName ?: "설정되지 않음"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ProfileInfoCard(
                        title = "사용자명",
                        value = currentUser.username ?: "설정되지 않음"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ProfileInfoCard(
                        title = "이메일",
                        value = currentUser.email
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ProfileInfoCard(
                        title = "프리미엄 상태",
                        value = if (currentUser.isPremium) "프리미엄 사용자" else "일반 사용자"
                    )
                    
                    if (currentUser.isPremium && currentUser.premiumUntil != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoCard(
                            title = "프리미엄 만료일",
                            value = formatDate(currentUser.premiumUntil)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 계정 삭제 버튼
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("계정 삭제")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentUser = uiState.user,
            onDismiss = { showEditDialog = false },
            onConfirm = { username, fullName ->
                viewModel.updateProfile(username, fullName)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                
                viewModel.deleteAccount(
                    onSuccess = {
                        // 계정 삭제 성공 시 로그인 화면으로 이동
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUser: com.memorygym.app.data.model.User?,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?) -> Unit
) {
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("프로필 수정")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("이름") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("사용자명 (선택사항)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        username.trim().ifBlank { null },
                        fullName.trim().ifBlank { null }
                    )
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "계정 삭제",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "정말로 계정을 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "이 작업은 되돌릴 수 없으며, 다음 데이터가 영구적으로 삭제됩니다:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• 모든 플래시카드\n• 학습 진도 및 통계\n• 프로필 정보\n• 학습 세션 기록",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    return timestamp?.let {
        val date = java.util.Date(it.seconds * 1000)
        java.text.SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.getDefault()).format(date)
    } ?: "날짜 없음"
} 