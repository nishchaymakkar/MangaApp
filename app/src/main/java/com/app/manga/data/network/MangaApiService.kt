package com.app.manga.data.network

import android.util.Log
import com.app.manga.data.model.Data
import com.app.manga.data.model.Manga
import retrofit2.http.GET
import retrofit2.http.Query

interface MangaApiService {
    @GET("manga/fetch")
    suspend fun getAllManga(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<Manga> {
        try {
            val response = getAllManga(page, size)
            Log.d("MangaApiService", "Success! Received ${response.size} items")
            return response
        } catch (e: Exception) {
            Log.e("MangaApiService", "Error fetching manga: ${e.message}", e)
            throw e
        }
    }
}