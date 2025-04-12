package com.app.manga.ui.screens.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.app.manga.ui.screens.mainscreen.facestreamscreen.FaceStreamScreen
import com.app.manga.ui.screens.mainscreen.mangascreen.MangaScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
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
        innerpadding ->
        NavHost(
            modifier = modifier.padding(innerpadding),
            navController = mainScreenNavController,
            startDestination = "face stream screen"
        ){
            composable(route = "manga screen") {
                MangaScreen()
            }
            composable(route = "face stream screen"){
                FaceStreamScreen()
            }
        }
    }


}