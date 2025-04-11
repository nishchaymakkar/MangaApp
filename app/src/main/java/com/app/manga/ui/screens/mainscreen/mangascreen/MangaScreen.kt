package com.app.manga.ui.screens.mainscreen.mangascreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import com.app.manga.data.model.Data
import org.koin.androidx.compose.koinViewModel

@Composable
fun MangaScreen(
    modifier: Modifier = Modifier,
    viewModel: MangaScreenViewModel = koinViewModel()
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
    
    Scaffold { innerpadding ->
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(count = manga.itemCount) { index ->
                                val item = manga[index]
                                Log.d("MangaScreen", "Rendering item at index $index: $item")
                                
                                if (item != null) {
                                    Column {
                                        Text(text = "Manga Item #$index")
                                        item.data.forEach { data ->
                                            MangaItem(data = data)
                                        }
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
}

@Composable
fun MangaItem(
    data: Data,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "ID: ${data.id}")
        Text(text = "Title: ${data.title}")
        Text(text = "Status: ${data.status}")
    }
}