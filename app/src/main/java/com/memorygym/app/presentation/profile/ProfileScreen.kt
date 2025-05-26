package com.memorygym.app.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
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
                    .padding(24.dp),
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

private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    return timestamp?.let {
        val date = java.util.Date(it.seconds * 1000)
        java.text.SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.getDefault()).format(date)
    } ?: "날짜 없음"
} 