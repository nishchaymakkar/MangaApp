@file:OptIn(ExperimentalPagingApi::class)

package com.app.manga.data.network

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaDatabase
import com.app.manga.data.toDataEntity
import com.app.manga.data.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MangaRemoteMediator(
    private val mangaDb: MangaDatabase,
    private val mangaApi: MangaApiService
): RemoteMediator<Int, DataEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DataEntity>
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
                        // Calculate next page based on total items loaded
                        (state.pages.size + 1)
                    }
                }
            }

            Log.d("MangaRemoteMediator", "Loading page $loadKey with page size ${state.config.pageSize}")
            
            val response = withContext(Dispatchers.IO) {
                try {
                    mangaApi.getAllManga(
                        page = loadKey,
                        size = state.config.pageSize
                    )
                } catch (e: HttpException) {
                    Log.e("MangaRemoteMediator", "HTTP error: ${e.code()}, ${e.message()}", e)
                    throw e
                } catch (e: IOException) {
                    Log.e("MangaRemoteMediator", "Network error: ${e.message}", e)
                    throw e
                }
            }
            
            if (!response.isSuccessful) {
                Log.e("MangaRemoteMediator", "API call unsuccessful: ${response.code()}")
                return MediatorResult.Error(HttpException(response))
            }

            val mangaList = response.body()?.data ?: emptyList()
            Log.d("MangaRemoteMediator", "API returned ${mangaList.size} manga items")
            
            mangaDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    mangaDb.dao.clearAll()
                    Log.d("MangaRemoteMediator", "Cleared local database for refresh")
                }
                
                val dataEntities = mangaList.map { it.toDataEntity() }
                mangaDb.dao.upsertManga(dataEntities)
                Log.d("MangaRemoteMediator", "Saved ${dataEntities.size} manga items to database")
            }

            MediatorResult.Success(
                endOfPaginationReached = mangaList.isEmpty()
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