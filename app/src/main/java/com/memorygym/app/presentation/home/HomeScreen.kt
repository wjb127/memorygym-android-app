package com.memorygym.app.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.Memory) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    // 에러 메시지 표시
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // 성공 메시지 표시
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (snackbarData.visuals.message.contains("추가되었습니다")) {
                            Color(0xFFE8F5E8) // 연한 녹색
                        } else {
                            Color(0xFFFFE8E8) // 연한 빨간색 (에러용)
                        },
                        contentColor = if (snackbarData.visuals.message.contains("추가되었습니다")) {
                            Color(0xFF2E7D32) // 진한 녹색 텍스트
                        } else {
                            Color(0xFFD32F2F) // 진한 빨간색 텍스트
                        }
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentColor = AccentPink,
                modifier = Modifier.height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = "앱 로고",
                        tint = AccentPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "암기훈련소 - 매일 훈련하는 두뇌는 더 강해집니다",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            TopSection(
                isUserSignedIn = uiState.currentUser != null,
                onLoginClick = {
                    if (uiState.isSignedOut) {
                        navController.navigate(Screen.Login.route)
                    } else {
                        // 이미 로그인 되어있다면 프로필 화면으로 보낼 수 있음, 또는 로그아웃 기능 제공
                        // 여기서는 로그인 상태면 프로필 클릭 유도
                    }
                 },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLogoutClick = { viewModel.signOut() } // 로그아웃 함수 호출
            )
            HomeTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            when (selectedTab) {
                HomeTab.Memory -> MemoryTabContent(viewModel, navController, uiState)
                HomeTab.Quiz -> QuizTabContent(navController, viewModel)
                HomeTab.Subject -> SubjectTabContent(navController, viewModel)
            }
        }
    }
}

@Composable
fun TopSection(isUserSignedIn: Boolean, onLoginClick: () -> Unit, onProfileClick: () -> Unit, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = "암기훈련소 로고",
                    tint = AccentPink,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "암기훈련소",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
            }
            if (isUserSignedIn) {
                 Row {
                     Button(
                        onClick = onProfileClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("내 정보", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onLogoutClick,
                        border = BorderStroke(1.dp, ButtonGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("로그아웃", color = TextGray, fontSize = 14.sp)
                    }
                 }
            } else {
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("로그인", color = TextGray, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "당신의 두뇌를 위한 최고의 트레이닝",
            fontSize = 14.sp,
            color = TextGray
        )
    }
}

enum class HomeTab(val title: String) {
    Memory("암기"),
    Quiz("퀴즈"),
    Subject("과목")
}

enum class QuizTab(val title: String) {
    Add("퀴즈 추가"),
    Manage("퀴즈 관리")
}

@Composable
fun QuizTabs(selectedTab: QuizTab, onTabSelected: (QuizTab) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.White,
        contentColor = AccentPink,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = AccentPink,
                height = 3.dp
            )
        }
    ) {
        QuizTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == tab) AccentPink else TextGray
                    )
                },
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTypeCard(
    icon: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentPink else LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else TextGray
            )
        }
    }
}

@Composable
fun HomeTabs(selectedTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.White,
        contentColor = AccentPink,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = AccentPink,
                height = 3.dp
            )
        }
    ) {
        HomeTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == tab) AccentPink else TextGray
                    )
                },
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@Composable
fun MemoryTabContent(viewModel: HomeViewModel, navController: NavController, uiState: HomeUiState) {
    var selectedSubject: com.memorygym.app.data.model.Subject? by remember { mutableStateOf(null) }
    val subjects by viewModel.subjects.collectAsState()
    var showSubjectDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LightGray)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = "두뇌 훈련하기 로고",
                    tint = AccentPink,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "두뇌 훈련하기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
            }
            IconButton(onClick = { /* TODO: 새로고침 로직, 예를 들어 viewModel.refreshSubjects() */ }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "새로고침", tint = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "학습할 과목 선택",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box {
            OutlinedButton(
                onClick = { showSubjectDropdown = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AccentPink.copy(alpha = 0.5f))
            ) {
                Text(
                    text = selectedSubject?.name ?: "과목 선택",
                    color = if (selectedSubject != null) TextGray else AccentPink.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "과목 목록 열기", tint = AccentPink.copy(alpha = 0.7f))
            }

            DropdownMenu(
                expanded = showSubjectDropdown,
                onDismissRequest = { showSubjectDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (subjects.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("과목이 없습니다.") },
                        onClick = { showSubjectDropdown = false },
                        enabled = false
                    )
                }
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject.name) },
                        onClick = {
                            selectedSubject = subject
                            showSubjectDropdown = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("+ 새 과목 추가", color = AccentPink) },
                    onClick = {
                        navController.navigate(Screen.Subjects.route) 
                        showSubjectDropdown = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedSubject == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "학습을 시작하려면 먼저 과목을 선택해주세요.",
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 40.dp, horizontal = 20.dp)
                        .fillMaxWidth()
                )
            }
        } else {
             Button(
                onClick = {
                    selectedSubject?.let { subject ->
                        navController.navigate(Screen.TrainingCenter.createRoute(subject.id, subject.name))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                shape = RoundedCornerShape(8.dp),
                enabled = uiState.currentUser != null
            ) {
                Text(
                    text = if (uiState.currentUser != null) "학습 시작" else "로그인 후 이용 가능", 
                    color = Color.White, 
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun QuizTabContent(navController: NavController, viewModel: HomeViewModel) {
    var selectedQuizTab by remember { mutableStateOf(QuizTab.Add) }
    var selectedSubject: com.memorygym.app.data.model.Subject? by remember { mutableStateOf(null) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    val subjects by viewModel.subjects.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 퀴즈 탭 (퀴즈 추가 / 퀴즈 관리)
        item {
            QuizTabs(selectedTab = selectedQuizTab, onTabSelected = { selectedQuizTab = it })
        }
        
        when (selectedQuizTab) {
            QuizTab.Add -> {
                // 두뇌 운동 퀴즈 추가 섹션
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) {
                                Text("🧠", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "두뇌 운동 퀴즈 추가",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                            }
                            
                            // 퀴즈 타입 선택
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuizTypeCard(
                                    icon = "🧩",
                                    title = "단일 퀴즈",
                                    isSelected = true,
                                    modifier = Modifier.weight(1f)
                                )
                                QuizTypeCard(
                                    icon = "💰",
                                    title = "퀴즈 구매",
                                    isSelected = false,
                                    onClick = {
                                        Toast.makeText(context, "아직 구현중입니다", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // 퀴즈를 추가할 과목 선택
                item {
                    Text(
                        text = "퀴즈를 추가할 과목 선택",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "새로고침",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 과목 선택 드롭다운
                    Box {
                        OutlinedButton(
                            onClick = { showSubjectDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, LightGray)
                        ) {
                            Text(
                                text = selectedSubject?.name ?: "과목 선택",
                                color = if (selectedSubject != null) TextGray else TextGray.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "과목 목록 열기", tint = TextGray.copy(alpha = 0.6f))
                        }

                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (subjects.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("과목이 없습니다.") },
                                    onClick = { showSubjectDropdown = false },
                                    enabled = false
                                )
                            }
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubject = subject
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 문제 입력
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text("❓", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "문제 (예: 영단어 뜻)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentPink
                                )
                            }
                            
                            OutlinedTextField(
                                value = question,
                                onValueChange = { question = it },
                                placeholder = { Text("문제가 되는 설명이나 힌트를 입력하세요", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPink,
                                    unfocusedBorderColor = LightGray
                                )
                            )
                        }
                    }
                }
                
                // 정답 입력
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text("💡", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "정답 (예: 영단어)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentPink
                                )
                            }
                            
                            OutlinedTextField(
                                value = answer,
                                onValueChange = { answer = it },
                                placeholder = { Text("정답이 되는 단어나 내용을 입력하세요", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPink,
                                    unfocusedBorderColor = LightGray
                                )
                            )
                        }
                    }
                }
                
                // 버튼들
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                question = ""
                                answer = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ButtonGray)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔄", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("초기화", color = TextGray, fontSize = 16.sp)
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (selectedSubject != null && question.isNotBlank() && answer.isNotBlank()) {
                                    viewModel.addQuiz(selectedSubject!!.id, question, answer)
                                    question = ""
                                    answer = ""
                                }
                            },
                            enabled = selectedSubject != null && question.isNotBlank() && answer.isNotBlank() && !uiState.isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSubject != null && question.isNotBlank() && answer.isNotBlank()) AccentPink else ButtonGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "추가 중...",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = if (selectedSubject == null) "과목을 선택해주세요" else "퀴즈 추가",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            
            QuizTab.Manage -> {
                // 퀴즈 관리 대시보드
                item {
                    var showSubjectDropdown by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) {
                                Text("📊", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "퀴즈 관리 대시보드",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPink
                                )
                            }
                            
                            // 검색 바
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("문제 또는 정답 검색", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = { 
                                        uiState.selectedManageSubject?.let { subject ->
                                            viewModel.setSearchQuery(uiState.searchQuery)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔍", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("검색", color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 과목 선택
                item {
                    var showSubjectDropdown by remember { mutableStateOf(false) }
                    
                    Text(
                        text = "과목 선택",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 과목 선택 드롭다운
                    Box {
                        OutlinedButton(
                            onClick = { showSubjectDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, LightGray)
                        ) {
                            Text(
                                text = uiState.selectedManageSubject?.name ?: "과목 선택",
                                color = if (uiState.selectedManageSubject != null) TextGray else TextGray.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "과목 목록 열기", tint = TextGray.copy(alpha = 0.6f))
                        }

                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (subjects.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("과목이 없습니다.") },
                                    onClick = { showSubjectDropdown = false },
                                    enabled = false
                                )
                            }
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        viewModel.setManageSubject(subject)
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 훈련소 선택 (과목이 선택된 경우에만 표시)
                if (uiState.selectedManageSubject != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text("🧠", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "훈련소 선택",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextGray
                                    )
                                }
                                
                                // 전체보기 버튼
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isAllSelected = uiState.selectedTrainingLevel == 0
                                    
                                    if (isAllSelected) {
                                        Button(
                                            onClick = { viewModel.setTrainingLevel(0) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("📋", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("전체보기", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("(${uiState.flashcards.size}개)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { viewModel.setTrainingLevel(0) },
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(1.dp, ButtonGray),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("📋", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("전체보기", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // 1-3단계 훈련소
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (1..3).forEach { level ->
                                        val isSelected = uiState.selectedTrainingLevel == level
                                        
                                        if (isSelected) {
                                            Button(
                                                onClick = { viewModel.setTrainingLevel(level) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("${level}단계", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.setTrainingLevel(level) },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, ButtonGray),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("${level}단계", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // 4-5단계 훈련소
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (4..5).forEach { level ->
                                        val isSelected = uiState.selectedTrainingLevel == level
                                        
                                        if (isSelected) {
                                            Button(
                                                onClick = { viewModel.setTrainingLevel(level) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("${level}단계", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.setTrainingLevel(level) },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, ButtonGray),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("${level}단계", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    
                                    // 빈 공간 채우기
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    
                    // 훈련소 정보
                    item {
                        val levelDescription = when (uiState.selectedTrainingLevel) {
                            0 -> "전체 퀴즈"
                            1 -> "매일 퀴즈"
                            2 -> "3일마다 퀴즈"
                            3 -> "일주일마다 퀴즈"
                            4 -> "2주마다 퀴즈"
                            5 -> "한달마다 퀴즈"
                            else -> ""
                        }
                        
                        val levelText = if (uiState.selectedTrainingLevel == 0) {
                            "전체보기: $levelDescription (${uiState.flashcards.size}개)"
                        } else {
                            "${uiState.selectedTrainingLevel}단계 훈련소: $levelDescription (${uiState.flashcards.size}개)"
                        }
                        
                        Text(
                            text = levelText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // 로딩 상태
                    if (uiState.isLoadingFlashcards) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AccentPink)
                            }
                        }
                    }
                    // 퀴즈 목록
                    else if (uiState.flashcards.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📝", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "이 훈련소에 퀴즈가 없습니다",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "퀴즈 추가 탭에서 새 퀴즈를 추가해보세요!",
                                        fontSize = 14.sp,
                                        color = TextGray.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.flashcards) { flashcard ->
                            var showEditDialog by remember { mutableStateOf(false) }
                            var editFront by remember { mutableStateOf("") }
                            var editBack by remember { mutableStateOf("") }
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    // 문제와 정답
                                    Text(
                                        text = "문제: ${flashcard.front}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    Text(
                                        text = "정답: ${flashcard.back}",
                                        fontSize = 14.sp,
                                        color = TextGray.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // 마지막 학습일 (임시로 생성일 표시)
                                    Text(
                                        text = "생성일: ${formatDate(flashcard.createdAt)}",
                                        fontSize = 12.sp,
                                        color = TextGray.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // 수정/삭제 버튼
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                editFront = flashcard.front
                                                editBack = flashcard.back
                                                showEditDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✏️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("수정", color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                        
                                        Button(
                                            onClick = { showDeleteDialog = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🗑️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("삭제", color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 수정 다이얼로그
                            if (showEditDialog) {
                                AlertDialog(
                                    onDismissRequest = { showEditDialog = false },
                                    title = { Text("퀴즈 수정") },
                                    text = {
                                        Column {
                                            Text("문제", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                            OutlinedTextField(
                                                value = editFront,
                                                onValueChange = { editFront = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("정답", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                            OutlinedTextField(
                                                value = editBack,
                                                onValueChange = { editBack = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.updateFlashcard(flashcard, editFront, editBack)
                                                showEditDialog = false
                                            },
                                            enabled = editFront.isNotBlank() && editBack.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                                        ) {
                                            Text("수정", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEditDialog = false }) {
                                            Text("취소")
                                        }
                                    }
                                )
                            }
                            
                            // 삭제 확인 다이얼로그
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("퀴즈 삭제") },
                                    text = { Text("정말로 이 퀴즈를 삭제하시겠습니까?\n삭제된 퀴즈는 복구할 수 없습니다.") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.deleteFlashcard(flashcard)
                                                showDeleteDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                                        ) {
                                            Text("삭제", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) {
                                            Text("취소")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectTabContent(navController: NavController, viewModel: HomeViewModel) {
    var subjectName by remember { mutableStateOf("") }
    var subjectDescription by remember { mutableStateOf("") }
    val subjects by viewModel.subjects.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 새 과목 추가 섹션
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "새 과목 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "과목 이름",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        placeholder = { Text("예: 영어 단어, 한국사, 프로그래밍 등", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "설명 (선택사항)",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = subjectDescription,
                        onValueChange = { subjectDescription = it },
                        placeholder = { Text("과목에 대한 간단한 설명을 입력하세요", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (subjectName.isNotBlank()) {
                                    viewModel.createSubject(
                                        subjectName.trim(),
                                        subjectDescription.trim().ifBlank { null }
                                    )
                                    subjectName = ""
                                    subjectDescription = ""
                                }
                            },
                            enabled = subjectName.isNotBlank() && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📚", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("과목 추가", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // 과목 관리 섹션 헤더
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "과목 관리",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                
                // 중급 영단어가 없는 경우에만 표시
                val hasIntermediateEnglish = subjects.any { it.name == "중급 영단어" }
                if (!hasIntermediateEnglish) {
                    Button(
                        onClick = { viewModel.createInitialDataForCurrentUser() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("생성 중...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Text("🎁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("중급 영단어 받기", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // 과목 목록 또는 빈 상태
        if (subjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📚",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "아직 과목이 없습니다",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "위에서 새 과목을 추가해보세요!",
                            fontSize = 14.sp,
                            color = TextGray.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(subjects) { subject ->
                SubjectManagementCard(
                    subject = subject,
                    onClick = {
                        navController.navigate(Screen.Flashcards.createRoute(subject.id, subject.name))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectManagementCard(
    subject: com.memorygym.app.data.model.Subject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSkyBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )

                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = subject.description ?: "설명이 없습니다",
                    fontSize = 14.sp,
                    color = TextGray.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "생성일: ${formatDate(subject.createdAt)}",
                    fontSize = 12.sp,
                    color = TextGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun QuizManageTabContent(
    navController: NavController,
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    subjects: List<com.memorygym.app.data.model.Subject>
) {
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingFlashcard by remember { mutableStateOf<com.memorygym.app.data.model.Flashcard?>(null) }
    var editFront by remember { mutableStateOf("") }
    var editBack by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingFlashcard by remember { mutableStateOf<com.memorygym.app.data.model.Flashcard?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 퀴즈 관리 대시보드
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Text("📊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "퀴즈 관리 대시보드",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPink
                        )
                    }
                    
                    // 검색 바
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("문제 또는 정답 검색", fontSize = 14.sp, color = TextGray.copy(alpha = 0.6f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = { 
                                uiState.selectedManageSubject?.let { subject ->
                                    viewModel.setSearchQuery(uiState.searchQuery)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔍", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("검색", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // 과목 선택
        item {
            Text(
                text = "과목 선택",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 과목 선택 드롭다운
            Box {
                OutlinedButton(
                    onClick = { showSubjectDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LightGray)
                ) {
                    Text(
                        text = uiState.selectedManageSubject?.name ?: "과목 선택",
                        color = if (uiState.selectedManageSubject != null) TextGray else TextGray.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "과목 목록 열기", tint = TextGray.copy(alpha = 0.6f))
                }

                DropdownMenu(
                    expanded = showSubjectDropdown,
                    onDismissRequest = { showSubjectDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (subjects.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("과목이 없습니다.") },
                            onClick = { showSubjectDropdown = false },
                            enabled = false
                        )
                    }
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                viewModel.setManageSubject(subject)
                                showSubjectDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        // 훈련소 선택 (과목이 선택된 경우에만 표시)
        if (uiState.selectedManageSubject != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("🧠", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "훈련소 선택",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextGray
                            )
                        }
                        
                                                 // 1-3단계 훈련소
                         Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             (1..3).forEach { level ->
                                 val isSelected = uiState.selectedTrainingLevel == level
                                 
                                 if (isSelected) {
                                     Button(
                                         onClick = { viewModel.setTrainingLevel(level) },
                                         modifier = Modifier.weight(1f),
                                         colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                         shape = RoundedCornerShape(8.dp)
                                     ) {
                                         Column(
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Text("${level}단계", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                             Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 } else {
                                     OutlinedButton(
                                         onClick = { viewModel.setTrainingLevel(level) },
                                         modifier = Modifier.weight(1f),
                                         border = BorderStroke(1.dp, ButtonGray),
                                         shape = RoundedCornerShape(8.dp)
                                     ) {
                                         Column(
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Text("${level}단계", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                             Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }
                             }
                         }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                                                 // 4-5단계 훈련소
                         Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             (4..5).forEach { level ->
                                 val isSelected = uiState.selectedTrainingLevel == level
                                 
                                 if (isSelected) {
                                     Button(
                                         onClick = { viewModel.setTrainingLevel(level) },
                                         modifier = Modifier.weight(1f),
                                         colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                         shape = RoundedCornerShape(8.dp)
                                     ) {
                                         Column(
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Text("${level}단계", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                             Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 } else {
                                     OutlinedButton(
                                         onClick = { viewModel.setTrainingLevel(level) },
                                         modifier = Modifier.weight(1f),
                                         border = BorderStroke(1.dp, ButtonGray),
                                         shape = RoundedCornerShape(8.dp)
                                     ) {
                                         Column(
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Text("${level}단계", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                             Text("${uiState.flashcards.filter { it.boxNumber == level }.size}", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }
                             }
                             
                             // 빈 공간 채우기
                             Spacer(modifier = Modifier.weight(1f))
                         }
                    }
                }
            }
            
            // 훈련소 정보
            item {
                val levelDescription = when (uiState.selectedTrainingLevel) {
                    1 -> "매일 퀴즈"
                    2 -> "3일마다 퀴즈"
                    3 -> "일주일마다 퀴즈"
                    4 -> "2주마다 퀴즈"
                    5 -> "한달마다 퀴즈"
                    else -> ""
                }
                
                Text(
                    text = "${uiState.selectedTrainingLevel}단계 훈련소: $levelDescription (${uiState.flashcards.size}개)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 로딩 상태
            if (uiState.isLoadingFlashcards) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPink)
                    }
                }
            }
            // 퀴즈 목록
            else if (uiState.flashcards.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "이 훈련소에 퀴즈가 없습니다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "퀴즈 추가 탭에서 새 퀴즈를 추가해보세요!",
                                fontSize = 14.sp,
                                color = TextGray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(uiState.flashcards) { flashcard ->
                    FlashcardManageItem(
                        flashcard = flashcard,
                        onEdit = {
                            editingFlashcard = flashcard
                            editFront = flashcard.front
                            editBack = flashcard.back
                            showEditDialog = true
                        },
                        onDelete = {
                            deletingFlashcard = flashcard
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // 수정 다이얼로그
    if (showEditDialog && editingFlashcard != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("퀴즈 수정") },
            text = {
                Column {
                    Text("문제", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editFront,
                        onValueChange = { editFront = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("정답", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editBack,
                        onValueChange = { editBack = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingFlashcard?.let { flashcard ->
                            viewModel.updateFlashcard(flashcard, editFront, editBack)
                        }
                        showEditDialog = false
                        editingFlashcard = null
                    },
                    enabled = editFront.isNotBlank() && editBack.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("수정", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog && deletingFlashcard != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("퀴즈 삭제") },
            text = { Text("정말로 이 퀴즈를 삭제하시겠습니까?\n삭제된 퀴즈는 복구할 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingFlashcard?.let { flashcard ->
                            viewModel.deleteFlashcard(flashcard)
                        }
                        showDeleteDialog = false
                        deletingFlashcard = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun FlashcardManageItem(
    flashcard: com.memorygym.app.data.model.Flashcard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 문제와 정답
            Text(
                text = "문제: ${flashcard.front}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "정답: ${flashcard.back}",
                fontSize = 14.sp,
                color = TextGray.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 마지막 학습일 (임시로 생성일 표시)
            Text(
                text = "생성일: ${formatDate(flashcard.createdAt)}",
                fontSize = 12.sp,
                color = TextGray.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 수정/삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✏️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("수정", color = Color.White, fontSize = 14.sp)
                    }
                }
                
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗑️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("삭제", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    return timestamp?.let {
        val date = it.toDate()
        val formatter = java.text.SimpleDateFormat("yyyy. M. d.", java.util.Locale.KOREAN)
        formatter.format(date)
    } ?: "알 수 없음"
} 