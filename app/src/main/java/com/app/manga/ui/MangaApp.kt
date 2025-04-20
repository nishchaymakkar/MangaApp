package com.app.manga.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.manga.ui.navigation.MainScreen
import com.app.manga.ui.navigation.SignInScreen
import com.app.manga.ui.screens.mainscreen.MainScreen
import com.app.manga.ui.screens.signinscreen.SignInScreen
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue

@Composable
fun MangaApp(
    modifier: Modifier = Modifier
) {
    val viewModel: MangaAppViewModel = koinViewModel()
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Only show NavHost when login state is determined
    isLoggedIn?.let { logged ->
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = if (logged) MainScreen else SignInScreen
        ) {
            composable<SignInScreen> {
                SignInScreen(onSignInClick = {
                    navController.navigate(MainScreen)
                })
            }
            composable<MainScreen> {
                MainScreen(
                    onSignOut = {
                        navController.navigate(SignInScreen) {
                            popUpTo(0)
                        }
                    }
                )

            }
        }
    }}