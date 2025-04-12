package com.app.manga.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.manga.ui.navigation.MainScreen
import com.app.manga.ui.navigation.SignInScreen
import com.app.manga.ui.screens.mainscreen.MainScreen
import com.app.manga.ui.screens.signinscreen.SignInScreen

@Composable
fun MangaApp(
    modifier: Modifier = Modifier
) {
   val navController = rememberNavController()
    val isLoggedIn = true

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainScreen else SignInScreen
    ){
        composable<SignInScreen> {
            SignInScreen()
        }
        composable<MainScreen> {
            MainScreen()

    }
    }
}