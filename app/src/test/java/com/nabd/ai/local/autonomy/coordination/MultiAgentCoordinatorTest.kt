package com.nabd.ai.local.autonomy.coordination

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MultiAgentCoordinatorTest {

    private lateinit var coordinator: MultiAgentCoordinator

    @Before
    fun setUp() {
        coordinator = MultiAgentCoordinator()
    }

    // ── Helper: Stub Agent ──────────────────────────────────

    private class StubAgent(
        override val id: String,
        override val role: AgentRole,
        private val responseContent: String = "stub response"
    ) : Agent {
        var lastReceivedMessage: AgentMessage? = null
        var processCallCount = 0

        override suspend fun process(message: AgentMessage): AgentResponse {
            lastReceivedMessage = message
            processCallCount++
            return AgentResponse(content = responseContent, metadata = mapOf("role" to role.name))
        }
    }

    // ── Registration Tests ──────────────────────────────────

    @Test
    fun `routeTask sends to correct agent by role`() = runBlocking {
        val planner = StubAgent("planner_1", AgentRole.PLANNER, "plan generated")
        val executor = StubAgent("executor_1", AgentRole.EXECUTOR, "execution done")

        coordinator.registerAgent(planner)
        coordinator.registerAgent(executor)

        val plannerResponse = coordinator.routeTask(AgentRole.PLANNER, "Create a plan")
        assertEquals("plan generated", plannerResponse.content)
        assertEquals("Create a plan", planner.lastReceivedMessage?.content)

        val executorResponse = coordinator.routeTask(AgentRole.EXECUTOR, "Execute step 1")
        assertEquals("execution done", executorResponse.content)
        assertEquals("Execute step 1", executor.lastReceivedMessage?.content)
    }

    @Test(expected = IllegalStateException::class)
    fun `routeTask throws when no agent for role`() = runBlocking {
        // No agents registered
        coordinator.routeTask(AgentRole.REVIEWER, "Review this")
        Unit
    }

    // ── Broadcast Tests ─────────────────────────────────────

    @Test
    fun `broadcast sends message to all registered agents`() = runBlocking {
        val planner = StubAgent("p", AgentRole.PLANNER)
        val executor = StubAgent("e", AgentRole.EXECUTOR)
        val reviewer = StubAgent("r", AgentRole.REVIEWER)

        coordinator.registerAgent(planner)
        coordinator.registerAgent(executor)
        coordinator.registerAgent(reviewer)

        val broadcastMsg = AgentMessage(senderId = "coordinator", content = "sync memory")
        coordinator.broadcast(broadcastMsg)

        assertEquals(1, planner.processCallCount)
        assertEquals(1, executor.processCallCount)
        assertEquals(1, reviewer.processCallCount)

        assertEquals("sync memory", planner.lastReceivedMessage?.content)
        assertEquals("sync memory", executor.lastReceivedMessage?.content)
        assertEquals("sync memory", reviewer.lastReceivedMessage?.content)
    }

    @Test
    fun `broadcast with no agents does not crash`() = runBlocking {
        // Should not throw
        coordinator.broadcast(AgentMessage(senderId = "test", content = "hello"))
    }

    // ── Agent Replacement Tests ─────────────────────────────

    @Test
    fun `registering agent with same id replaces previous`() = runBlocking {
        val agentV1 = StubAgent("planner_1", AgentRole.PLANNER, "v1")
        val agentV2 = StubAgent("planner_1", AgentRole.PLANNER, "v2")

        coordinator.registerAgent(agentV1)
        coordinator.registerAgent(agentV2) // Should replace v1

        val response = coordinator.routeTask(AgentRole.PLANNER, "test")
        assertEquals("v2", response.content)
    }
}
