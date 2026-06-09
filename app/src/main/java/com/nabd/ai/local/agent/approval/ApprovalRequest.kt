package com.nabd.ai.local.agent.approval

data class ApprovalRequest(
    val id: String,
    val toolName: String,
    val description: String,
    val diffPreview: String? = null
)
