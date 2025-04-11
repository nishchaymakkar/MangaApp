@file:OptIn(ExperimentalPagingApi::class)

package com.app.manga.data.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaDatabase
import com.app.manga.data.local.database.MangaEntity
import com.app.manga.data.network.MangaApiService
import com.app.manga.data.network.MangaRemoteMediator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object AppModule {
    @ExperimentalPagingApi
    fun provideProductPager(mangaDb: MangaDatabase, mangaApi: MangaApiService): Pager<Int, MangaEntity> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 3
            ),
            remoteMediator = MangaRemoteMediator(
                mangaDb = mangaDb,
                mangaApi = mangaApi
            ),
            pagingSourceFactory = {
                mangaDb.dao.pagingSource()
            }
        )
    }
}

val appModule = module {
    single<MangaDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MangaDatabase::class.java,
            "manga_db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<MangaDatabase>().dao }

    single<Pager<Int, MangaEntity>> { AppModule.provideProductPager(get(), get()) }
}