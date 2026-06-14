package com.nabd.ai.agora.ui.generation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nabd.ai.agora.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenerationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val contextWindowSize: StateFlow<Int> = repository.contextWindowSize
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 20
        )

    val visualizeContext: StateFlow<Boolean> = repository.visualizeContext
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isThinkingEnabled: StateFlow<Boolean> = repository.isThinkingEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val thinkingLevel: StateFlow<String> = repository.thinkingLevel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Medium"
        )

    fun saveContextWindowSize(size: Int) {
        viewModelScope.launch {
            repository.saveContextWindowSize(size)
        }
    }

    fun saveVisualizeContext(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveVisualizeContext(enabled)
        }
    }

    fun saveThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveThinkingEnabled(enabled)
        }
    }

    fun saveThinkingLevel(level: String) {
        viewModelScope.launch {
            repository.saveThinkingLevel(level)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(GenerationViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return GenerationViewModel(app) as T
        }
    }
}
