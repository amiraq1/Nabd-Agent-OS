package com.nabd.ai.local.core.health

data class HealthReport(
    val isHealthy: Boolean,
    val issues: List<String>
)
