package com.nabd.ai.agora.ui.provider

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nabd.ai.agora.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProviderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    // Configuration Status flows
    val nvidiaKeyConfigured: StateFlow<Boolean> = repository.nvidiaApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val googleConfigured: StateFlow<Boolean> = repository.googleApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val openAIConfigured: StateFlow<Boolean> = repository.openAiApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val anthropicConfigured: StateFlow<Boolean> = repository.anthropicApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val deepSeekConfigured: StateFlow<Boolean> = repository.deepSeekApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val qwenConfigured: StateFlow<Boolean> = repository.qwenApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val openRouterConfigured: StateFlow<Boolean> = repository.openRouterApiKey
        .map { it.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Current Key flows
    val currentNvidiaKey: StateFlow<String> = repository.nvidiaApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentGoogleKey: StateFlow<String> = repository.googleApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentOpenAiKey: StateFlow<String> = repository.openAiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentAnthropicKey: StateFlow<String> = repository.anthropicApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentDeepSeekKey: StateFlow<String> = repository.deepSeekApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentQwenKey: StateFlow<String> = repository.qwenApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentOpenRouterKey: StateFlow<String> = repository.openRouterApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // Save mutators
    fun saveNvidiaKey(key: String) {
        viewModelScope.launch { repository.setNvidiaApiKey(key) }
    }

    fun saveGoogleKey(key: String) {
        viewModelScope.launch { repository.saveGoogleKey(key) }
    }

    fun saveOpenAiKey(key: String) {
        viewModelScope.launch { repository.saveOpenAiKey(key) }
    }

    fun saveAnthropicKey(key: String) {
        viewModelScope.launch { repository.saveAnthropicKey(key) }
    }

    fun saveDeepSeekKey(key: String) {
        viewModelScope.launch { repository.saveDeepSeekKey(key) }
    }

    fun saveQwenKey(key: String) {
        viewModelScope.launch { repository.saveQwenKey(key) }
    }

    fun saveOpenRouterKey(key: String) {
        viewModelScope.launch { repository.saveOpenRouterKey(key) }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProviderViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return ProviderViewModel(app) as T
        }
    }
}
