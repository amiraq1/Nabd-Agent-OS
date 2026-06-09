package com.nabd.ai.local.agent.approval

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ApprovalManager {
    private val _pendingRequest = MutableStateFlow<ApprovalRequest?>(null)
    val pendingRequest: StateFlow<ApprovalRequest?> = _pendingRequest.asStateFlow()

    fun requestApproval(toolName: String, description: String, diffPreview: String? = null): ApprovalRequest {
        val request = ApprovalRequest(
            id = UUID.randomUUID().toString(),
            toolName = toolName,
            description = description,
            diffPreview = diffPreview
        )
        _pendingRequest.value = request
        return request
    }

    fun resolveApproval(requestId: String) {
        if (_pendingRequest.value?.id == requestId) {
            _pendingRequest.value = null
        }
    }
}
