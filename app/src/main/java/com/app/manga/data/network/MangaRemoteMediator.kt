@file:OptIn(ExperimentalPagingApi::class)

package com.app.manga.data.network

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.withTransaction
import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaDatabase
import com.app.manga.data.local.database.MangaEntity
import com.app.manga.data.toDataEntity
import com.app.manga.data.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MangaRemoteMediator(
    private val mangaDb: MangaDatabase,
    private val mangaApi: MangaApiService
):  RemoteMediator<Int, MangaEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MangaEntity>
    ): MediatorResult {
        return try {
            Log.d("MangaRemoteMediator", "Load called with loadType: $loadType")
            
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(
                    endOfPaginationReached = true
                )
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        (lastItem.data.size / state.config.pageSize) + 1
                    }
                }
            }

            Log.d("MangaRemoteMediator", "Loading page $loadKey with page size ${state.config.pageSize}")
            
            val products = withContext(Dispatchers.IO) {
                try {
                    mangaApi.getAllManga(
                        page = loadKey.toInt(),
                        size = state.config.pageSize
                    )
                } catch (e: HttpException) {
                    Log.e("MangaRemoteMediator", "HTTP error: ${e.code()}, ${e.message()}", e)
                    Log.e("MangaRemoteMediator", "Response: ${e.response()?.errorBody()?.string()}")
                    throw e
                } catch (e: IOException) {
                    Log.e("MangaRemoteMediator", "Network error: ${e.message}", e)
                    throw e
                } catch (e: Exception) {
                    Log.e("MangaRemoteMediator", "Unknown error: ${e.message}", e)
                    throw e
                }
            }
            
            Log.d("MangaRemoteMediator", "API returned ${products.size} manga items")
            
            mangaDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    mangaDb.dao.clearAll()
                    Log.d("MangaRemoteMediator", "Cleared local database for refresh")
                }
                val mangaEntities = products.map { it.toEntity() }
                mangaDb.dao.upsertManga(mangaEntities)
                Log.d("MangaRemoteMediator", "Saved ${mangaEntities.size} manga items to database")
            }

            MediatorResult.Success(
                endOfPaginationReached = products.isEmpty()
            )
        } catch (e: HttpException) {
            Log.e("MangaRemoteMediator", "HTTP error in load(): ${e.code()}, ${e.message()}", e)
            MediatorResult.Error(e)
        } catch (e: IOException) {
            Log.e("MangaRemoteMediator", "IO error in load(): ${e.message}", e)
            MediatorResult.Error(e)
        } catch (e: Exception) {
            Log.e("MangaRemoteMediator", "Error in load(): ${e.message}", e)
            MediatorResult.Error(e)
        }
    }
}