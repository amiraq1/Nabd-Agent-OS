package com.nabd.ai.local.workspace

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class WorkspaceEventType {
    CREATED, MODIFIED, DELETED, RENAMED
}

data class WorkspaceEvent(
    val type: WorkspaceEventType,
    val relativePath: String,
    val timestamp: Long = System.currentTimeMillis()
)

class WorkspaceObserver {
    private val _events = MutableSharedFlow<WorkspaceEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<WorkspaceEvent> = _events.asSharedFlow()

    fun notifyEvent(type: WorkspaceEventType, path: String) {
        _events.tryEmit(WorkspaceEvent(type, path))
    }
}
