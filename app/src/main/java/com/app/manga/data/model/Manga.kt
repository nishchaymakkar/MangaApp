package com.app.manga.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class Manga(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val data: List<Data>
)