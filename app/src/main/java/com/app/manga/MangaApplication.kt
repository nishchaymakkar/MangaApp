package com.app.manga

import android.app.Application
import com.app.manga.data.di.appModule
import com.app.manga.data.di.networkModule
import com.app.manga.ui.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MangaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MangaApplication)
            androidLogger()
            modules(listOf(networkModule, appModule, viewModelModule))
        }

    }
}