package com.nabd.ai.local.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabd.ai.local.data.SettingsRepository
import com.nabd.ai.local.domain.ModelManager
import com.nabd.ai.local.domain.model.LocalModel
import com.nabd.ai.local.domain.model.ModelImportState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.nabd.ai.local.memory.db.MemoryDatabase
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import com.nabd.ai.local.rag.ingestion.KnowledgeIngestionManager

data class SettingsUiState(
    val models: List<LocalModel> = emptyList(),
    val activeModelName: String? = null,
    val importState: ModelImportState = ModelImportState.Idle,
    val documents: List<KnowledgeDocumentEntity> = emptyList()
)

class SettingsViewModel(
    private val modelManager: ModelManager,
    private val settingsRepository: SettingsRepository,
    private val ingestionManager: KnowledgeIngestionManager? = null,
    private val database: MemoryDatabase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe models
        modelManager.scanModels()
            .onEach { models -> _uiState.update { it.copy(models = models) } }
            .launchIn(viewModelScope)

        // Observe documents
        database?.knowledgeDao()?.getAllDocuments()?.onEach { docs ->
            _uiState.update { it.copy(documents = docs) }
        }?.launchIn(viewModelScope)

        // Observe active model
        settingsRepository.activeModelName
            .onEach { name -> _uiState.update { it.copy(activeModelName = name) } }
            .launchIn(viewModelScope)

        modelManager.importState
            .onEach { state -> 
                _uiState.update { it.copy(importState = state) }
                if (state is ModelImportState.Completed) {
                    refreshModels()
                }
            }
            .launchIn(viewModelScope)
    }

    fun importDocument(uri: Uri, title: String) {
        viewModelScope.launch {
            ingestionManager?.ingestDocument(uri, title)
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            ingestionManager?.deleteDocument(id)
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            modelManager.importModel(uri)
        }
    }

    fun selectModel(model: LocalModel) {
        viewModelScope.launch {
            settingsRepository.setActiveModel(model.fileName, model.path)
        }
    }

    fun deleteModel(model: LocalModel) {
        viewModelScope.launch {
            modelManager.deleteModel(model)
            if (_uiState.value.activeModelName == model.fileName) {
                settingsRepository.clearActiveModel()
            }
            refreshModels()
        }
    }

    private fun refreshModels() {
        viewModelScope.launch {
            modelManager.scanModels().collect { models ->
                _uiState.update { it.copy(models = models) }
            }
        }
    }
}
