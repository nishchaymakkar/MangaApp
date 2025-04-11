package com.app.manga.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("authors")
    val authors: List<String>,
    @SerialName("create_at")
    val createAt: Long,
    @SerialName("genres")
    val genres: List<String>,
    @SerialName("id")
    val id: String,
    @SerialName("nsfw")
    val nsfw: Boolean,
    @SerialName("status")
    val status: String,
    @SerialName("sub_title")
    val subTitle: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("thumb")
    val thumb: String,
    @SerialName("title")
    val title: String,
    @SerialName("total_chapter")
    val totalChapter: Int,
    @SerialName("type")
    val type: String,
    @SerialName("update_at")
    val updateAt: Long
)