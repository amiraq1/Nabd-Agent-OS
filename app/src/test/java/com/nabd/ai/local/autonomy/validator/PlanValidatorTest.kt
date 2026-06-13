package com.nabd.ai.local.autonomy.validator

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PlanValidatorTest {

    private lateinit var validator: DefaultPlanValidator
    private lateinit var availableTools: Set<String>

    @Before
    fun setUp() {
        validator = DefaultPlanValidator()
        availableTools = setOf("READ_FILE", "WRITE_FILE", "WEB_SEARCH", "CALCULATOR")
    }

    @Test
    fun testValidPlan() = runBlocking {
        val step1Id = UUID.randomUUID()
        val step2Id = UUID.randomUUID()

        val step1 = PlanStep(
            id = step1Id,
            toolName = "READ_FILE",
            arguments = emptyMap(),
            dependencies = emptyList(),
            requiredPermissions = emptyList()
        )

        val step2 = PlanStep(
            id = step2Id,
            toolName = "WRITE_FILE",
            arguments = emptyMap(),
            dependencies = listOf(step1Id),
            requiredPermissions = emptyList()
        )

        val plan = ExecutionPlan(UUID.randomUUID(), listOf(step1, step2))
        val result = validator.validate(plan, availableTools)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testToolUnavailable() = runBlocking {
        val stepId = UUID.randomUUID()
        val step = PlanStep(
            id = stepId,
            toolName = "UNAVAILABLE_TOOL",
            arguments = emptyMap(),
            dependencies = emptyList(),
            requiredPermissions = emptyList()
        )

        val plan = ExecutionPlan(UUID.randomUUID(), listOf(step))
        val result = validator.validate(plan, availableTools)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        val error = result.errors.first() as ValidationError.ToolUnavailable
        assertEquals(stepId, error.stepId)
        assertEquals("UNAVAILABLE_TOOL", error.toolName)
    }

    @Test
    fun testMissingDependency() = runBlocking {
        val stepId = UUID.randomUUID()
        val missingId = UUID.randomUUID()

        val step = PlanStep(
            id = stepId,
            toolName = "READ_FILE",
            arguments = emptyMap(),
            dependencies = listOf(missingId),
            requiredPermissions = emptyList()
        )

        val plan = ExecutionPlan(UUID.randomUUID(), listOf(step))
        val result = validator.validate(plan, availableTools)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        val error = result.errors.first() as ValidationError.MissingDependency
        assertEquals(stepId, error.stepId)
        assertEquals(missingId, error.missingId)
    }

    @Test
    fun testCircularDependency() = runBlocking {
        val step1Id = UUID.randomUUID()
        val step2Id = UUID.randomUUID()

        val step1 = PlanStep(
            id = step1Id,
            toolName = "READ_FILE",
            arguments = emptyMap(),
            dependencies = listOf(step2Id),
            requiredPermissions = emptyList()
        )

        val step2 = PlanStep(
            id = step2Id,
            toolName = "WRITE_FILE",
            arguments = emptyMap(),
            dependencies = listOf(step1Id),
            requiredPermissions = emptyList()
        )

        val plan = ExecutionPlan(UUID.randomUUID(), listOf(step1, step2))
        val result = validator.validate(plan, availableTools)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is ValidationError.CircularDependency })
    }

    @Test
    fun testDuplicateStepId() = runBlocking {
        val sameId = UUID.randomUUID()

        val step1 = PlanStep(
            id = sameId,
            toolName = "READ_FILE",
            arguments = emptyMap(),
            dependencies = emptyList(),
            requiredPermissions = emptyList()
        )

        val step2 = PlanStep(
            id = sameId,
            toolName = "WRITE_FILE",
            arguments = emptyMap(),
            dependencies = emptyList(),
            requiredPermissions = emptyList()
        )

        val plan = ExecutionPlan(UUID.randomUUID(), listOf(step1, step2))
        val result = validator.validate(plan, availableTools)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is ValidationError.DuplicateStepId })
    }
}
