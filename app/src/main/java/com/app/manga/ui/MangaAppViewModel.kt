package com.app.manga.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manga.data.local.datastore.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MangaAppViewModel(
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    val isLoggedIn = dataStoreRepository.isLoggedInFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun refreshLoginStatus() {
        viewModelScope.launch {
            dataStoreRepository.isLoggedIn()
        }
    }
}