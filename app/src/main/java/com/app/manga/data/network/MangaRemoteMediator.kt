@file:OptIn(ExperimentalPagingApi::class)

package com.app.manga.data.network

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

class MangaRemoteMediator(
    private val mangaDb: MangaDatabase,
    private val mangaApi: MangaApiService
):  RemoteMediator<Int, MangaEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MangaEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 0
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

            val products = mangaApi.getAllManga(
                page = loadKey.toInt(),
                size = state.config.pageSize
            )
            mangaDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    mangaDb.dao.clearAll()
                }
                val mangaEntities = products.map { it.toEntity() }
                mangaDb.dao.upsertManga(mangaEntities)
            }

            MediatorResult.Success(
                endOfPaginationReached = products.isEmpty()
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}