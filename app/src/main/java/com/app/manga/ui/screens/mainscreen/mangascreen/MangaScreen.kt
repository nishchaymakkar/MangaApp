package com.app.manga.ui.screens.mainscreen.mangascreen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.manga.data.model.Data
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(
    modifier: Modifier = Modifier,
    viewModel: MangaScreenViewModel = koinViewModel(),
    onSignOut: () -> Unit
) {
    val manga = viewModel.productPagingFlow.collectAsLazyPagingItems()
    val context = LocalContext.current
    
    LaunchedEffect(key1 = manga.loadState) {
        val refreshState = manga.loadState.refresh
        if(refreshState is LoadState.Error) {
            val error = refreshState.error
            Log.e("MangaScreen", "Error loading data: ${error.message}", error)
            Toast.makeText(
                context,
                "Error: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    var profileDialog by remember { mutableStateOf(false) }
    var selectedManga by remember { mutableStateOf<Data?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Manga App") },
                actions = {
                    IconButton(onClick = { profileDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "person"
                        )
                    }
                }
            )
        }
    ) { innerpadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerpadding)) {

            when (val state = manga.loadState.refresh) {
                is LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Log.d("MangaScreen", "Loading state displayed")
                }
                is LoadState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.error.localizedMessage}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is LoadState.NotLoading -> {
                    if (manga.itemCount == 0) {
                        Text(
                            text = "No manga found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Log.d("MangaScreen", "No items to display")
                    } else {
                        LazyVerticalGrid (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(count = manga.itemCount) { index ->
                                val item = manga[index]
                                
                                if (item != null) {
                                    Column {
                                        MangaItem(
                                            data = item,
                                            onClick = { selectedManga = item }
                                        )
                                    }
                                }
                            }
                            
                            item {
                                if (manga.loadState.append is LoadState.Loading) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add the detail dialog
    selectedManga?.let { manga ->
        MangaDetailDialog(
            manga = manga,
            onDismiss = { selectedManga = null }
        )
    }
    
    if (profileDialog) {
        ProfileDialog(
            onDismiss = {
                profileDialog = false
                viewModel.signOut()
                onSignOut()
            }
        )
    }

}

@Composable
fun MangaItem(
    data: Data,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data.thumb)
                .crossfade(true)
                .build(),
            contentDescription = data.title,
            modifier = modifier.aspectRatio(3/4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun MangaDetailDialog(
    manga: Data,
    onDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = manga.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.thumb)
                    .crossfade(true)
                    .build(),
                contentDescription = manga.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3/4f)
            )
            
            Text("Type: ${manga.type}")
            Text("Status: ${manga.status}")
            Text("Author: ${manga.authors}")
            Text("Genres: ${manga.genres}")
            Text("Total Chapter: ${manga.totalChapter}")
            Text("Summary: ${manga.summary}")
            Text("Update At: ${manga.updateAt}")
            Text("Sub Title: ${manga.subTitle}")
        }
    }
}

@Composable
fun ProfileDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile") },
        text = { Text("Are you sure you want to sign out?") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "person")
                    Text(text = "Sign Out")
                }
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}