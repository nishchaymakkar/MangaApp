package com.app.manga.ui.screens.mainscreen.mangascreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.cachedIn
import androidx.paging.map
import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaEntity
import com.app.manga.data.toData
import com.app.manga.data.toManga
import kotlinx.coroutines.flow.map

class MangaScreenViewModel(
    pager: Pager<Int, DataEntity>
): ViewModel() {
    val productPagingFlow = pager
        .flow
        .map { pagingData->
            pagingData.map {it.toData() }
        }
        .cachedIn(viewModelScope)
}