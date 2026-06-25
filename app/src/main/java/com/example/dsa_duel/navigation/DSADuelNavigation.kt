package com.example.dsa_duel.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsa_duel.screens.auth.AnalyticsScreen
import com.example.dsa_duel.screens.auth.ForgotPasswordScreen
import com.example.dsa_duel.screens.auth.LoginScreen
import com.example.dsa_duel.screens.auth.RegisterScreen
import com.example.dsa_duel.screens.auth.ResetPasswordScreen
import com.example.dsa_duel.screens.auth.duel.DuelScreen
import com.example.dsa_duel.screens.auth.home.HomeScreen
import com.example.dsa_duel.screens.auth.profile.ProfileScreen
import com.example.dsa_duel.screens.auth.ranking.RankingScreen
import com.example.dsa_duel.viewModels.HomeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DSADuelNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uiState by homeViewModel.uiState.collectAsState()

    // Auth Guard: Redirect to Login if profile is missing on initial launch
    LaunchedEffect(uiState.isProfileMissing) {
        if (uiState.isProfileMissing) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 6 } },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 6 } }
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { user ->
                    homeViewModel.initializeProfile(
                        displayName = user.fullName ?: "Warrior",
                        email = user.email ?: "player@dsaduel.com"
                    )
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    homeViewModel.initializeProfile(
                        displayName = user.fullName ?: "Warrior",
                        email = user.email ?: "player@dsaduel.com"
                    )
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResetPassword = { email ->
                    navController.navigate("${Routes.RESET_PASSWORD}/$email")
                }
            )
        }

        composable(
            route = "${Routes.RESET_PASSWORD}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                email = email,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateDuel = { navController.navigate(Routes.DUEL) },
                onNavigateRank = { navController.navigate(Routes.RANK) },
                onNavigateProfile = { navController.navigate(Routes.PROFILE) },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.DUEL) {
            DuelScreen(onNavigateHome = { navController.popBackStack(Routes.HOME, false) })
        }

        composable(Routes.RANK) {
            RankingScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateAnalytics = { navController.navigate(Routes.ANALYTICS) },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }
    }
}
