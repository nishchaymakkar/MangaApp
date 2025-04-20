package com.app.manga.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_credentials")

class DataStoreRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val  EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
    }

    val emailFlow = dataStore.data.map { preferences ->
        preferences[EMAIL]
    }
    val passwordFlow = dataStore.data.map { preferences ->
        preferences[PASSWORD]
    }

    suspend fun saveCredentials(email: String, password: String) {
        dataStore.edit { preferences ->
            preferences[EMAIL] = email
            preferences[PASSWORD] = password
        }
    }
    suspend fun clearCredentials() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    suspend fun getEmail(): String? {
        return emailFlow.first()
    }
    suspend fun getPassword(): String? {
        return passwordFlow.first()
    }
    suspend fun isLoggedIn(): Boolean {
        return emailFlow.first() != null && passwordFlow.first() != null
    }
    fun isLoggedInFlow() = dataStore.data.map { preferences ->
        val email = preferences[EMAIL]
        val password = preferences[PASSWORD]
        !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }
}