package com.nabd.ai.local.autonomy.coordination

import com.nabd.ai.local.autonomy.planning.TaskPlanner
import com.nabd.ai.local.autonomy.planning.ExecutionPlan
import com.nabd.ai.local.agent.orchestrator.ToolOrchestrator
import com.nabd.ai.local.autonomy.reflection.ReflectionEngine
import com.nabd.ai.local.autonomy.replanning.ReplanningManager
import com.nabd.ai.local.autonomy.planning.PlanStep
import com.nabd.ai.local.autonomy.planning.StepState

/**
 * PlannerAgent: Responsible for decomposing high-level goals into actionable plans.
 */
class PlannerAgent(
    override val id: String = "agent_planner",
    private val planner: TaskPlanner
) : Agent {
    override val role: AgentRole = AgentRole.PLANNER

    override suspend fun process(message: AgentMessage): AgentResponse {
        val goal = message.content
        val plan = planner.generatePlan(goal, emptyList())
        return AgentResponse(content = "Plan generated with ${plan.steps.size} steps.", metadata = mapOf("plan" to plan))
    }
}

/**
 * ExecutorAgent: Responsible for executing individual plan steps using tools.
 */
class ExecutorAgent(
    override val id: String = "agent_executor",
    private val orchestrator: ToolOrchestrator
) : Agent {
    override val role: AgentRole = AgentRole.EXECUTOR

    override suspend fun process(message: AgentMessage): AgentResponse {
        orchestrator.runLoop(message.content)
        val trace = orchestrator.trace.value.entries.joinToString("\n") { "${it.type}: ${it.content}" }
        return AgentResponse(content = trace)
    }
}

/**
 * ReviewerAgent: Responsible for verifying the output of the ExecutorAgent.
 */
class ReviewerAgent(
    override val id: String = "agent_reviewer",
    private val reflectionEngine: ReflectionEngine
) : Agent {
    override val role: AgentRole = AgentRole.REVIEWER

    override suspend fun process(message: AgentMessage): AgentResponse {
        // In a real implementation, we'd need the PlanStep object from context
        val step = message.context["step"] as? PlanStep ?: return AgentResponse("ERROR: No step context")
        val observation = message.content
        
        val eval = reflectionEngine.evaluateStep(step, observation)
        val status = if (eval.isSuccess) "SUCCESS" else "FAILURE"
        
        return AgentResponse(content = "$status: ${eval.reasoning}", metadata = mapOf("eval" to eval))
    }
}

/**
 * ReflectionAgent: Responsible for analyzing failures and suggesting improvements.
 */
class ReflectionAgent(
    override val id: String = "agent_reflection",
    private val replanningManager: ReplanningManager
) : Agent {
    override val role: AgentRole = AgentRole.REFLECTION

    override suspend fun process(message: AgentMessage): AgentResponse {
        // Placeholder for deep reflection logic
        return AgentResponse(content = "Reflection complete. Suggesting strategy updates.")
    }
}
