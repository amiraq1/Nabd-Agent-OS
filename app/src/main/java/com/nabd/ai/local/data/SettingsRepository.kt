package com.nabd.ai.local.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nabd_settings")

/**
 * SettingsRepository: Single source of truth for application-level persistence.
 * Uses Preferences DataStore to store active model selection.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_MODEL_PATH = stringPreferencesKey("active_model_path")
        val ACTIVE_MODEL_NAME = stringPreferencesKey("active_model_name")
    }

    val activeModelPath: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.ACTIVE_MODEL_PATH] }

    val activeModelName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.ACTIVE_MODEL_NAME] }

    suspend fun setActiveModel(name: String, path: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_MODEL_PATH] = path
            preferences[Keys.ACTIVE_MODEL_NAME] = name
        }
    }

    suspend fun clearActiveModel() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_MODEL_PATH)
            preferences.remove(Keys.ACTIVE_MODEL_NAME)
        }
    }
}
