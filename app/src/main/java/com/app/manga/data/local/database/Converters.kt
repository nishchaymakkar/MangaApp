package com.app.manga.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromDataEntityList(list: List<DataEntity>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toDataEntityList(data: String): List<DataEntity> {
        val type = object : TypeToken<List<DataEntity>>() {}.type
        return gson.fromJson(data, type)
    }
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

