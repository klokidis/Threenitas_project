package com.example.threenitas_project

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.threenitas_project.network.ApiViewModel
import com.example.threenitas_project.ui.Magazines
import com.example.threenitas_project.ui.theme.BottomNavCurve

enum class BottomBarScreensNames {
    Magazine,
    Scan,
    Center,
    Profile,
    Settings
}

@Composable
fun BottomBarNavGraph(
    apiViewModel: ApiViewModel,
    navController: NavHostController = rememberNavController(),
) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = BottomBarScreensNames.valueOf(
        backStackEntry?.destination?.route ?: BottomBarScreensNames.Magazine.name
    )

    LaunchedEffect(currentScreen) {
        val newIndex = when (currentScreen) {
            BottomBarScreensNames.Magazine -> 0
            BottomBarScreensNames.Scan -> 1
            BottomBarScreensNames.Center -> 2
            BottomBarScreensNames.Profile -> 3
            BottomBarScreensNames.Settings -> 4
        }

        if (selectedItemIndex != newIndex) {
            selectedItemIndex = newIndex
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.BottomCenter
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(BottomNavCurve()),
                    containerColor = Color.White,
                    contentColor = Color.Transparent
                ) {
                    BottomBarScreensNames.entries.forEachIndexed { index, screen ->
                        if (screen == BottomBarScreensNames.Center) {
                            Spacer(modifier = Modifier.width(50.dp)) // Center space for FloatingActionButton
                        } else {
                            NavigationBarItem(
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                ),
                                modifier = Modifier
                                    .offset(
                                        y = 20.dp,
                                        x = if (screen.name == BottomBarScreensNames.Magazine.name || screen.name == BottomBarScreensNames.Scan.name) 10.dp else (-10).dp
                                    ),
                                icon = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = getIcon(screen)),
                                        contentDescription = screen.name,
                                        tint = if (selectedItemIndex == index) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.name,
                                        color = if (selectedItemIndex == index) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                selected = selectedItemIndex == index,
                                onClick = {
                                    selectedItemIndex = index
                                    navController.navigateToScreen(index)
                                }
                            )
                        }
                    }
                }

                // FloatingActionButton in the center
                FloatingActionButton(
                    onClick = {
                        selectedItemIndex = 2
                        navController.navigate(BottomBarScreensNames.Center.name)
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .offset(y = (10).dp)
                        .align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.play_arrow_24px),
                        contentDescription = stringResource(R.string.play),
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            modifier = Modifier.padding(),
            startDestination = BottomBarScreensNames.Magazine.name,
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            composable(route = BottomBarScreensNames.Magazine.name) {
                Magazines(
                    apiViewModel,
                    bottomPadding = padding
                )
            }
            composable(route = BottomBarScreensNames.Scan.name) { Box(modifier = Modifier.fillMaxSize()) }
            composable(route = BottomBarScreensNames.Center.name) { Box(modifier = Modifier.fillMaxSize()) }
            composable(route = BottomBarScreensNames.Profile.name) { Box(modifier = Modifier.fillMaxSize()) }
            composable(route = BottomBarScreensNames.Settings.name) { Box(modifier = Modifier.fillMaxSize()) }
        }
    }
}

private fun NavHostController.navigateToScreen(index: Int) {
    val screen = when (index) {
        0 -> BottomBarScreensNames.Magazine.name
        1 -> BottomBarScreensNames.Scan.name
        2 -> BottomBarScreensNames.Center.name
        3 -> BottomBarScreensNames.Profile.name
        else -> BottomBarScreensNames.Settings.name
    }
    navigate(screen) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun getIcon(screen: BottomBarScreensNames): Int {
    return when (screen) {
        BottomBarScreensNames.Magazine -> R.drawable.book
        BottomBarScreensNames.Scan -> R.drawable.qr_code_scanner_24px
        BottomBarScreensNames.Center -> R.drawable.book
        BottomBarScreensNames.Profile -> R.drawable.person_24px
        BottomBarScreensNames.Settings -> R.drawable.settings_24px
    }
}
