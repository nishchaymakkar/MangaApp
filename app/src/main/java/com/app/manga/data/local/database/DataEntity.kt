package com.app.manga.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataEntity(
    val authors: List<String>,
    val createAt: Long,
    val genres: List<String>,
    @PrimaryKey
    val id: String,
    val nsfw: Boolean,
    val status: String,
    val subTitle: String?,
    val summary: String,
    val thumb: String,
    val title: String,
    val totalChapter: Int,
    val type: String,
    val updateAt: Long
)

@Entity
data class MangaEntity(
    @PrimaryKey val code: Int,
    val data: List<DataEntity>
)