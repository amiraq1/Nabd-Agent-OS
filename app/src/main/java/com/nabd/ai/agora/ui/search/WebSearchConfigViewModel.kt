package com.nabd.ai.agora.ui.search

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

class WebSearchConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val isWebSearchEnabled: StateFlow<Boolean> = repository.isWebSearchEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val braveSearchApiKey: StateFlow<String> = repository.braveSearchApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val maxSearchResults: StateFlow<Int> = repository.maxSearchResults
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 5
        )

    fun toggleWebSearch(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveWebSearchStatus(enabled)
        }
    }

    fun saveBraveKey(key: String) {
        viewModelScope.launch {
            repository.saveBraveSearchApiKey(key)
        }
    }

    fun saveMaxResults(count: Int) {
        viewModelScope.launch {
            repository.saveMaxSearchResults(count)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WebSearchConfigViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return WebSearchConfigViewModel(app) as T
        }
    }
}
