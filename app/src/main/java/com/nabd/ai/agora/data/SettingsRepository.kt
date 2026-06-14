package com.nabd.ai.agora.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nabd_system_config"
)

class SettingsRepository private constructor(context: Context) {

    private val dataStore = context.dataStore

    val isLocalEngine: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_IS_LOCAL_ENGINE] ?: true }

    val threadCount: Flow<Int> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_THREAD_COUNT] ?: 4 }

    val nvidiaApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_NVIDIA_API_KEY] ?: "" }

    suspend fun setIsLocalEngine(value: Boolean) {
        dataStore.edit { it[KEY_IS_LOCAL_ENGINE] = value }
    }

    suspend fun setThreadCount(value: Int) {
        dataStore.edit { it[KEY_THREAD_COUNT] = value.coerceIn(1, 16) }
    }

    suspend fun setNvidiaApiKey(value: String) {
        dataStore.edit { it[KEY_NVIDIA_API_KEY] = value.trim() }
    }

    companion object {
        private val KEY_IS_LOCAL_ENGINE = booleanPreferencesKey("is_local_engine")
        private val KEY_THREAD_COUNT    = intPreferencesKey("thread_count")
        private val KEY_NVIDIA_API_KEY  = stringPreferencesKey("nvidia_api_key")

        @Volatile private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}