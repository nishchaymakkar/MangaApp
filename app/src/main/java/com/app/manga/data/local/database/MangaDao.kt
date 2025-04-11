package com.app.manga.data.local.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MangaDao {
    @Upsert
    suspend fun upsertManga(manga: List<MangaEntity>)

    @Query("SELECT * FROM MangaEntity")
    fun pagingSource(): PagingSource<Int, MangaEntity>

    @Query("DELETE FROM MangaEntity")
    suspend fun clearAll()

}