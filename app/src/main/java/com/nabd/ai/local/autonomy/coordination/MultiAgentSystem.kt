package com.nabd.ai.local.autonomy.coordination

import java.util.UUID

/**
 * Agent: Interface for specialized intelligence roles.
 */
interface Agent {
    val id: String
    val role: AgentRole
    suspend fun process(message: AgentMessage): AgentResponse
}

enum class AgentRole { PLANNER, EXECUTOR, REVIEWER, REFLECTION }

/**
 * AgentMessage: Structured communication between agents.
 */
data class AgentMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val content: String,
    val context: Map<String, Any> = emptyMap()
)

data class AgentResponse(
    val content: String,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * MultiAgentCoordinator: Orchestrates communication and task routing between specialized agents.
 */
class MultiAgentCoordinator {
    private val agents = mutableMapOf<String, Agent>()

    fun registerAgent(agent: Agent) {
        agents[agent.id] = agent
    }

    suspend fun routeTask(targetRole: AgentRole, request: String): AgentResponse {
        val agent = agents.values.find { it.role == targetRole } 
            ?: throw IllegalStateException("No agent found for role: $targetRole")
            
        return agent.process(AgentMessage(senderId = "coordinator", content = request))
    }

    /**
     * Broadcast a message to all registered agents (e.g., for shared memory synchronization).
     */
    suspend fun broadcast(message: AgentMessage) {
        agents.values.forEach { it.process(message) }
    }
}
