package com.app.manga.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manga.data.local.datastore.DataStoreRepository
import kotlinx.coroutines.launch

class MangaAppViewModel (
    private val dataStoreRepository: DataStoreRepository
): ViewModel(){

    val email = dataStoreRepository.emailFlow
}