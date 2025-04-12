package com.app.manga.ui.navigation.navigationitems

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Face
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)


val items = listOf(
    NavigationItem(
        title = "Manga Screen",
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book
    ),
    NavigationItem(
        title = "Face Stream Screen",
        selectedIcon = Icons.Filled.Face,
        unselectedIcon = Icons.Outlined.Face
    ),
)