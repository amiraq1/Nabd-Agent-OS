package com.nabd.ai.agora.ui.tools

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

class ToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val isWebSearchEnabled: StateFlow<Boolean> = repository.isWebSearchEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isConversationSearchEnabled: StateFlow<Boolean> = repository.isConversationSearchEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isShellExecutionEnabled: StateFlow<Boolean> = repository.isShellExecutionEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun toggleWebSearch(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveWebSearchStatus(enabled)
        }
    }

    fun toggleConversationSearch(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveConversationSearchStatus(enabled)
        }
    }

    fun toggleShellExecution(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveShellExecutionStatus(enabled)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ToolsViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return ToolsViewModel(app) as T
        }
    }
}
