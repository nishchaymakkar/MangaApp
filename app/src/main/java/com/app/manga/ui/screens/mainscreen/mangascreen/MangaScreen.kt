package com.app.manga.ui.screens.mainscreen.mangascreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.app.manga.data.model.Data
import kotlinx.serialization.builtins.LongArraySerializer
import org.koin.androidx.compose.koinViewModel

@Composable
fun MangaScreen(
    modifier: Modifier = Modifier,
    viewModel: MangaScreenViewModel = koinViewModel()
) {
    val manga = viewModel.productPagingFlow.collectAsLazyPagingItems()
    val context = LocalContext.current
    LaunchedEffect(key1 = manga.loadState) {
        if(manga.loadState.refresh is LoadState.Error) {
            Toast.makeText(
                context,
                "Error: " + (manga.loadState.refresh as LoadState.Error).error.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Scaffold { innerpadding ->
        Box(modifier
            .fillMaxSize()
            .padding(innerpadding)){
            if (manga.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier.align(Alignment.Center)
                )
            }else {

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(count = manga.itemCount){index ->
                        val item = manga[index]
                        item?.let { it.data.map { MangaItem(data = it) } }

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


@Composable
fun MangaItem(
    data: Data,
    modifier: Modifier = Modifier) {

    Text(
        text = "${data.id}, ${data.title}"
    )

}