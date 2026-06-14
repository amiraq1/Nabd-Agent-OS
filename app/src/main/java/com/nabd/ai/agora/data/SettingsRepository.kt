package com.nabd.ai.agora.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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

    /* ── Phase 3 Flows ────────────────────────────────────────── */
    val isLocalEngine: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_IS_LOCAL_ENGINE] ?: true }

    val threadCount: Flow<Int> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_THREAD_COUNT] ?: 4 }

    val nvidiaApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_NVIDIA_API_KEY] ?: "" }

    /* ── Phase 5 Flows ────────────────────────────────────────── */
    val googleApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_GOOGLE_API_KEY] ?: "" }

    val openAiApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_OPENAI_API_KEY] ?: "" }

    val anthropicApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_ANTHROPIC_API_KEY] ?: "" }

    val deepSeekApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_DEEPSEEK_API_KEY] ?: "" }

    val qwenApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_QWEN_API_KEY] ?: "" }

    val openRouterApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_OPENROUTER_API_KEY] ?: "" }

    /* ── Phase 6 Flows ────────────────────────────────────────── */
    val isWebSearchEnabled: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_TOOL_WEB_SEARCH] ?: false }

    val isConversationSearchEnabled: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_TOOL_CONV_SEARCH] ?: false }

    val isShellExecutionEnabled: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_TOOL_SHELL_EXEC] ?: false }

    /* ── Phase 8 & 9 Flows ────────────────────────────────────── */
    val contextWindowSize: Flow<Int> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { (it[KEY_CONTEXT_WINDOW] ?: 20).coerceIn(1, 100) }

    val visualizeContext: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_VISUALIZE_CONTEXT] ?: false }

    val isThinkingEnabled: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_THINKING_ENABLED] ?: false }

    val thinkingLevel: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_THINKING_LEVEL] ?: "Medium" }

    val braveSearchApiKey: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_BRAVE_SEARCH_KEY] ?: "" }

    val maxSearchResults: Flow<Int> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { (it[KEY_MAX_SEARCH_RESULTS] ?: 5).coerceIn(1, 10) }

    /* ── Phase 10 Flows ────────────────────────────────────────── */
    val activeProvider: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_ACTIVE_PROVIDER] ?: "OpenAI" }

    val activeModel: Flow<String> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_ACTIVE_MODEL] ?: "gpt-4o" }

    /* ── Mutators ─────────────────────────────────────────────── */
    suspend fun setIsLocalEngine(value: Boolean) {
        dataStore.edit { it[KEY_IS_LOCAL_ENGINE] = value }
    }

    suspend fun setThreadCount(value: Int) {
        dataStore.edit { it[KEY_THREAD_COUNT] = value.coerceIn(1, 16) }
    }

    suspend fun setNvidiaApiKey(value: String) {
        dataStore.edit { it[KEY_NVIDIA_API_KEY] = value.trim() }
    }

    suspend fun saveGoogleKey(key: String) {
        dataStore.edit { it[KEY_GOOGLE_API_KEY] = key.trim() }
    }

    suspend fun saveOpenAiKey(key: String) {
        dataStore.edit { it[KEY_OPENAI_API_KEY] = key.trim() }
    }

    suspend fun saveAnthropicKey(key: String) {
        dataStore.edit { it[KEY_ANTHROPIC_API_KEY] = key.trim() }
    }

    suspend fun saveDeepSeekKey(key: String) {
        dataStore.edit { it[KEY_DEEPSEEK_API_KEY] = key.trim() }
    }

    suspend fun saveQwenKey(key: String) {
        dataStore.edit { it[KEY_QWEN_API_KEY] = key.trim() }
    }

    suspend fun saveOpenRouterKey(key: String) {
        dataStore.edit { it[KEY_OPENROUTER_API_KEY] = key.trim() }
    }

    suspend fun saveWebSearchStatus(enabled: Boolean) {
        dataStore.edit { it[KEY_TOOL_WEB_SEARCH] = enabled }
    }

    suspend fun saveConversationSearchStatus(enabled: Boolean) {
        dataStore.edit { it[KEY_TOOL_CONV_SEARCH] = enabled }
    }

    suspend fun saveShellExecutionStatus(enabled: Boolean) {
        dataStore.edit { it[KEY_TOOL_SHELL_EXEC] = enabled }
    }

    suspend fun saveContextWindowSize(size: Int) {
        dataStore.edit { it[KEY_CONTEXT_WINDOW] = size.coerceIn(1, 100) }
    }

    suspend fun saveVisualizeContext(enabled: Boolean) {
        dataStore.edit { it[KEY_VISUALIZE_CONTEXT] = enabled }
    }

    suspend fun saveThinkingEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_THINKING_ENABLED] = enabled }
    }

    suspend fun saveThinkingLevel(level: String) {
        require(level in listOf("High", "Medium", "Low"))
        dataStore.edit { it[KEY_THINKING_LEVEL] = level }
    }

    suspend fun saveBraveSearchApiKey(key: String) {
        dataStore.edit { it[KEY_BRAVE_SEARCH_KEY] = key.trim() }
    }

    suspend fun saveMaxSearchResults(count: Int) {
        dataStore.edit { it[KEY_MAX_SEARCH_RESULTS] = count.coerceIn(1, 10) }
    }

    suspend fun saveActiveProvider(value: String) {
        dataStore.edit { it[KEY_ACTIVE_PROVIDER] = value.trim() }
    }

    suspend fun saveActiveModel(value: String) {
        dataStore.edit { it[KEY_ACTIVE_MODEL] = value.trim() }
    }

    companion object {
        /* ── Phase 3 Keys ────────────────────────────────────────── */
        private val KEY_IS_LOCAL_ENGINE = booleanPreferencesKey("is_local_engine")
        private val KEY_THREAD_COUNT    = intPreferencesKey("thread_count")
        private val KEY_NVIDIA_API_KEY  = stringPreferencesKey("nvidia_api_key")

        /* ── Phase 5 Keys ────────────────────────────────────────── */
        val KEY_GOOGLE_API_KEY    = stringPreferencesKey("google_api_key")
        val KEY_OPENAI_API_KEY    = stringPreferencesKey("openai_api_key")
        val KEY_ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val KEY_DEEPSEEK_API_KEY  = stringPreferencesKey("deepseek_api_key")
        val KEY_QWEN_API_KEY      = stringPreferencesKey("qwen_api_key")
        val KEY_OPENROUTER_API_KEY= stringPreferencesKey("openrouter_api_key")

        /* ── Phase 6 Keys ────────────────────────────────────────── */
        val KEY_TOOL_WEB_SEARCH  = booleanPreferencesKey("tool_web_search")
        val KEY_TOOL_CONV_SEARCH = booleanPreferencesKey("tool_conv_search")
        val KEY_TOOL_SHELL_EXEC  = booleanPreferencesKey("tool_shell_exec")

        /* ── Phase 8 & 9 Keys ────────────────────────────────────── */
        val KEY_CONTEXT_WINDOW     = intPreferencesKey("context_window_size")
        val KEY_VISUALIZE_CONTEXT  = booleanPreferencesKey("visualize_context_rollout")
        val KEY_THINKING_ENABLED   = booleanPreferencesKey("thinking_enabled")
        val KEY_THINKING_LEVEL     = stringPreferencesKey("thinking_level")
        val KEY_BRAVE_SEARCH_KEY   = stringPreferencesKey("brave_search_api_key")
        val KEY_MAX_SEARCH_RESULTS = intPreferencesKey("max_search_results")

        /* ── Phase 10 Keys ───────────────────────────────────────── */
        val KEY_ACTIVE_PROVIDER = stringPreferencesKey("active_cloud_provider")
        val KEY_ACTIVE_MODEL    = stringPreferencesKey("active_cloud_model")

        @Volatile private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
