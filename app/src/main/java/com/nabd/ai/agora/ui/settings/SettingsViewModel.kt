package com.nabd.ai.agora.ui.settings

import android.app.Application
import androidx.lifecycle.*
import com.nabd.ai.agora.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val isLocalEngine: StateFlow<Boolean> = repository.isLocalEngine.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = true
    )

    val threadCount: StateFlow<Int> = repository.threadCount.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = 4
    )

    val nvidiaApiKey: StateFlow<String> = repository.nvidiaApiKey.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = ""
    )

    fun updateEngineMode(isLocal: Boolean) {
        viewModelScope.launch { repository.setIsLocalEngine(isLocal) }
    }

    fun updateThreadCount(count: Int) {
        viewModelScope.launch { repository.setThreadCount(count) }
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch { repository.setNvidiaApiKey(key) }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return SettingsViewModel(app) as T
        }
    }
}