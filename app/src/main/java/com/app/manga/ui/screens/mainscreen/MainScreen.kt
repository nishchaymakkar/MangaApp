package com.app.manga.ui.screens.mainscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.manga.ui.navigation.navigationitems.items
import com.app.manga.ui.screens.mainscreen.facestreamscreen.facedetectorlive.FaceDetectorScreen
import com.app.manga.ui.screens.mainscreen.mangascreen.MangaScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit
) {
    val mainScreenNavController = rememberNavController()
    val navBackStackEntry by mainScreenNavController.currentBackStackEntryAsState()
    Scaffold (
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = item.title.lowercase() ==
                            navBackStackEntry?.destination?.route
                    NavigationBarItem(
                        selected = isSelected,
                        label = {
                            Text(text = item.title)
                        },
                        icon = {
                            Icon(
                                imageVector = if(isSelected) {
                                    item.selectedIcon
                                } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        onClick = {
                            mainScreenNavController.navigate(item.title.lowercase()) {
                                popUpTo(mainScreenNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }}
        }
    ){
        NavHost(
            modifier = modifier.padding(),
            navController = mainScreenNavController,
            startDestination = "manga screen"
        ){
            composable(route = "manga screen") {
                MangaScreen(
                    onSignOut = onSignOut
                )
            }
            composable(route = "face stream screen"){
                FaceDetectorScreen()
            }

        }
    }


}



