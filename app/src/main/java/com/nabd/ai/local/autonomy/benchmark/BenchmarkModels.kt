package com.nabd.ai.local.autonomy.benchmark

import java.util.UUID

/**
 * BenchmarkCase: A single test scenario for the agent.
 */
data class BenchmarkCase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val goal: String,
    val initialContext: String = "",
    val expectedOutcome: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val categories: List<String> = emptyList()
)

enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }

/**
 * BenchmarkResult: The outcome of a single benchmark case run.
 */
data class BenchmarkResult(
    val caseId: String,
    val isSuccess: Boolean,
    val actualOutcome: String,
    val durationMs: Long,
    val replanningCount: Int,
    val toolUsageCount: Int,
    val tokensUsed: Int = 0,
    val score: Float // 0.0 to 1.0
)

/**
 * BenchmarkReport: Aggregated results of a benchmark run.
 */
data class BenchmarkReport(
    val runId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val results: List<BenchmarkResult>,
    val averageScore: Float,
    val successRate: Float,
    val totalDurationMs: Long
)
