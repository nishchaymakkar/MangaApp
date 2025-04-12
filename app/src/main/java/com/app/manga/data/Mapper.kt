package com.app.manga.data

import com.app.manga.data.local.database.DataEntity
import com.app.manga.data.local.database.MangaEntity
import com.app.manga.data.model.Data
import com.app.manga.data.model.Manga
import kotlin.random.Random


fun Data.toDataEntity() = DataEntity(
    authors = authors,
    createAt = createAt,
    genres = genres,
    id = id,
    nsfw = nsfw,
    status = status,
    subTitle = subTitle,
    summary = summary,
    thumb = thumb,
    title = title,
    totalChapter = totalChapter,
    type = type,
    updateAt = updateAt
)
fun DataEntity.toData() = Data(
    authors = authors,
    createAt = createAt,
    genres = genres,
    id = id,
    nsfw = nsfw,
    status = status,
    subTitle = subTitle ?: "No Subtitle",
    summary = summary,
    thumb = thumb,
    title = title,
    totalChapter = totalChapter,
    type = type,
    updateAt = updateAt
)

fun Manga.toEntity() = MangaEntity(
    code = code,
    data = data.map { it.toDataEntity() }
)

fun MangaEntity.toManga() = Manga(
    code = code,
    data = data.map { it.toData() }
)