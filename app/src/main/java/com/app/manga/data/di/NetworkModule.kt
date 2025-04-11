package com.app.manga.data.di

import android.content.Context
import com.app.manga.data.di.NetworkModule.provideBlogifyApiService
import com.app.manga.data.di.NetworkModule.provideOkHttpClient
import com.app.manga.data.di.NetworkModule.provideRetrofit
import com.app.manga.data.network.MangaApiService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://rapidapi.com/sagararofie/api/mangaverse-api"
object NetworkModule {
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }


    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideBlogifyApiService(retrofit: Retrofit): MangaApiService {
        return retrofit.create(MangaApiService::class.java)
    }
}
val networkModule= module {
    single { provideOkHttpClient(get())}
    single { provideRetrofit(get()) }
    single { provideBlogifyApiService(get()) }
}