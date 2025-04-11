@file:OptIn(ExperimentalPagingApi::class)

package com.app.manga.data.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import com.app.manga.data.di.AppModule.provideProductPager
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
        return   Pager(
            config = PagingConfig(pageSize = 10),
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
    single { provideProductPager(get(), get()) }
    single<MangaDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MangaDatabase::class.java,
            "manga_db"
        ).fallbackToDestructiveMigration(dropAllTables = false).build()
    }

    // Provide DAO
    single { get<MangaDatabase>().dao }


    // Pager
    single {
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { get<MangaDatabase>().dao.pagingSource() }
        )
    }
}