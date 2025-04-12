package com.app.manga.ui.di

import com.app.manga.ui.screens.mainscreen.mangascreen.MangaScreenViewModel
import com.app.manga.ui.screens.signinscreen.SignInViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MangaScreenViewModel(get()) }
    viewModel { SignInViewModel() }
}