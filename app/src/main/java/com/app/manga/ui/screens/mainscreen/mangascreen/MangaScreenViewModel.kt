package com.app.manga.ui.screens.mainscreen.mangascreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.cachedIn
import androidx.paging.map
import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaEntity
import com.app.manga.data.local.datastore.DataStoreRepository
import com.app.manga.data.toData
import com.app.manga.data.toManga
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MangaScreenViewModel(
    pager: Pager<Int, DataEntity>,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {
    val productPagingFlow = pager
        .flow
        .map { pagingData->
            pagingData.map {it.toData() }
        }
        .cachedIn(viewModelScope)


    fun signOut(){
        viewModelScope.launch {
            dataStoreRepository.clearCredentials()
        }
    }
}