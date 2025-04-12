package com.app.manga.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.manga.data.model.Manga

@Database(
    entities = [MangaEntity::class, DataEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MangaDatabase : RoomDatabase(){
     abstract val dao: MangaDao
}