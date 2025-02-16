package com.example.threenitas_project

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.threenitas_project.network.ApiViewModel
import com.example.threenitas_project.ui.signIn.SignIn

enum class AppScreens {
    SignIn,
    BottomBarScreens
}

@Composable
fun NavigationScreen(
    navController: NavHostController = rememberNavController(),
    apiViewModel: ApiViewModel = viewModel(factory = ApiViewModel.Factory)
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.SignIn.name,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) },
        ) {
            composable(route = AppScreens.SignIn.name) {
                SignIn(
                    apiViewModel = apiViewModel,
                    navigateToBottomBar = { navController.navigate(AppScreens.BottomBarScreens.name) }
                )
            }
            composable(route = AppScreens.BottomBarScreens.name) {
                BottomBarNavGraph(apiViewModel)
            }
        }
    }
}