package com.app.manga.data.local.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MangaDao {
    @Upsert
    suspend fun upsertManga(manga: List<DataEntity>)

    @Query("SELECT * FROM DataEntity")
    fun pagingSource(): PagingSource<Int, DataEntity>

    @Query("DELETE FROM DataEntity")
    suspend fun clearAll()

}