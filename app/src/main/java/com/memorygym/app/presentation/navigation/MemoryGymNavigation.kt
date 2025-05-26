package com.memorygym.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.memorygym.app.presentation.auth.LoginScreen
import com.memorygym.app.presentation.feedback.FeedbackScreen
import com.memorygym.app.presentation.flashcard.FlashcardListScreen
import com.memorygym.app.presentation.home.HomeScreen
import com.memorygym.app.presentation.profile.ProfileScreen
import com.memorygym.app.presentation.splash.SplashScreen
import com.memorygym.app.presentation.statistics.StatisticsScreen
import com.memorygym.app.presentation.study.StudyScreen
import com.memorygym.app.presentation.subject.SubjectListScreen
import com.memorygym.app.presentation.training.TrainingCenterScreen
import com.memorygym.app.presentation.training.TrainingStudyScreen
import com.memorygym.app.presentation.training.TrainingResultScreen

@Composable
fun MemoryGymNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Subjects.route) {
            SubjectListScreen(navController = navController)
        }
        
        composable(
            route = Screen.Flashcards.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            FlashcardListScreen(
                navController = navController,
                subjectId = subjectId,
                subjectName = subjectName
            )
        }
        
        composable(
            route = Screen.Study.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            StudyScreen(
                navController = navController,
                subjectId = subjectId,
                subjectName = subjectName
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }
        
        composable(Screen.Feedback.route) {
            FeedbackScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(
            route = Screen.TrainingCenter.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            TrainingCenterScreen(
                navController = navController,
                subjectId = subjectId,
                subjectName = subjectName
            )
        }
        
        composable(
            route = Screen.TrainingStudy.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("trainingLevel") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            val trainingLevel = backStackEntry.arguments?.getInt("trainingLevel") ?: 1
            TrainingStudyScreen(
                navController = navController,
                subjectId = subjectId,
                subjectName = subjectName,
                trainingLevel = trainingLevel
            )
        }
        
        composable(
            route = Screen.TrainingResult.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("trainingLevel") { type = NavType.IntType },
                navArgument("correctCount") { type = NavType.IntType },
                navArgument("incorrectCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            val trainingLevel = backStackEntry.arguments?.getInt("trainingLevel") ?: 1
            val correctCount = backStackEntry.arguments?.getInt("correctCount") ?: 0
            val incorrectCount = backStackEntry.arguments?.getInt("incorrectCount") ?: 0
            TrainingResultScreen(
                navController = navController,
                trainingLevel = trainingLevel,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                subjectId = subjectId,
                subjectName = subjectName
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Subjects : Screen("subjects")
    object Flashcards : Screen("flashcards/{subjectId}/{subjectName}") {
        fun createRoute(subjectId: String, subjectName: String) = 
            "flashcards/$subjectId/$subjectName"
    }
    object Profile : Screen("profile")
    object Study : Screen("study/{subjectId}/{subjectName}") {
        fun createRoute(subjectId: String, subjectName: String) = 
            "study/$subjectId/$subjectName"
    }
    object Statistics : Screen("statistics")
    object Feedback : Screen("feedback")
    object TrainingCenter : Screen("training_center/{subjectId}/{subjectName}") {
        fun createRoute(subjectId: String, subjectName: String) = 
            "training_center/$subjectId/$subjectName"
    }
    object TrainingStudy : Screen("training_study/{subjectId}/{subjectName}/{trainingLevel}") {
        fun createRoute(subjectId: String, subjectName: String, trainingLevel: Int) = 
            "training_study/$subjectId/$subjectName/$trainingLevel"
    }
    object TrainingResult : Screen("training_result/{subjectId}/{subjectName}/{trainingLevel}/{correctCount}/{incorrectCount}") {
        fun createRoute(subjectId: String, subjectName: String, trainingLevel: Int, correctCount: Int, incorrectCount: Int) = 
            "training_result/$subjectId/$subjectName/$trainingLevel/$correctCount/$incorrectCount"
    }
} 