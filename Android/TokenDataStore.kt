package com.stockagecloud.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "stockage_cloud_prefs")

class TokenDataStore(private val context: Context) {

    private val cleToken = stringPreferencesKey("jwt_token")

    val token: Flow<String?> = context.dataStore.data.map { prefs -> prefs[cleToken] }

    suspend fun sauvegarderToken(token: String) {
        context.dataStore.edit { prefs -> prefs[cleToken] = token }
    }

    suspend fun effacerToken() {
        context.dataStore.edit { prefs -> prefs.remove(cleToken) }
    }
}
