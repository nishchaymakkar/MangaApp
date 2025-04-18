package com.app.manga.data.di

import android.R.attr.level
import android.content.Context
import com.app.manga.data.di.NetworkModule.provideMangaApiService
import com.app.manga.data.di.NetworkModule.provideOkHttpClient
import com.app.manga.data.di.NetworkModule.provideRetrofit
import com.app.manga.data.network.MangaApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://mangaverse-api.p.rapidapi.com/"

object NetworkModule {
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", "42792d397bmshd63cc9437a37c81p17f393jsn59cbe2d96f6d")
                    .addHeader("x-rapidapi-host", "mangaverse-api.p.rapidapi.com")
                    .build()
                chain.proceed(request)
            }
            .build()
    }


    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideMangaApiService(retrofit: Retrofit): MangaApiService {
        return retrofit.create(MangaApiService::class.java)
    }
}

val networkModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideMangaApiService(get()) }
}