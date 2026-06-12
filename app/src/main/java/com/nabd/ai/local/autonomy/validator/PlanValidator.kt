package com.nabd.ai.local.autonomy.validator

import java.util.UUID

data class PlanStep(
    val id: UUID,
    val toolName: String,
    val arguments: Map<String, Any>,
    val dependencies: List<UUID>,
    val requiredPermissions: List<String>
)

data class ExecutionPlan(
    val id: UUID,
    val steps: List<PlanStep>
)

sealed interface ValidationError {
    data class DuplicateStepId(val id: UUID) : ValidationError
    data class MissingDependency(val stepId: UUID, val missingId: UUID) : ValidationError
    data class CircularDependency(val cycle: List<UUID>) : ValidationError
    data class ToolUnavailable(val stepId: UUID, val toolName: String) : ValidationError
    data class PermissionRequired(val stepId: UUID, val permissions: List<String>) : ValidationError
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>
)

interface PlanValidator {
    suspend fun validate(plan: ExecutionPlan, availableTools: Set<String>): ValidationResult
}

class DefaultPlanValidator : PlanValidator {
    override suspend fun validate(plan: ExecutionPlan, availableTools: Set<String>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val stepMap = plan.steps.associateBy { it.id }
        
        // 1. تحقق من تكرار المعرفات
        val duplicateIds = plan.steps.groupBy { it.id }.filter { it.value.size > 1 }.keys
        duplicateIds.forEach { errors.add(ValidationError.DuplicateStepId(it)) }

        // 2. تحقق من التبعيات المفقودة والأدوات المتاحة
        plan.steps.forEach { step ->
            if (!availableTools.contains(step.toolName)) {
                errors.add(ValidationError.ToolUnavailable(step.id, step.toolName))
            }
            step.dependencies.forEach { depId ->
                if (!stepMap.containsKey(depId)) {
                    errors.add(ValidationError.MissingDependency(step.id, depId))
                }
            }
        }

        // 3. كشف الحلقات المغلقة (Circular Dependencies) باستخدام الكشف عن الألوان (DFS)
        if (detectCycle(plan.steps, stepMap)) {
            errors.add(ValidationError.CircularDependency(findCyclePath(plan.steps, stepMap)))
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    private fun detectCycle(steps: List<PlanStep>, stepMap: Map<UUID, PlanStep>): Boolean {
        val visited = mutableMapOf<UUID, Int>() // 0: Unvisited, 1: Visiting, 2: Visited
        for (step in steps) {
            if (hasCycleDFS(step.id, stepMap, visited)) return true
        }
        return false
    }

    private fun hasCycleDFS(id: UUID, stepMap: Map<UUID, PlanStep>, visited: MutableMap<UUID, Int>): Boolean {
        if (visited[id] == 1) return true
        if (visited[id] == 2) return false
        
        visited[id] = 1
        val step = stepMap[id]
        step?.dependencies?.forEach { depId ->
            if (hasCycleDFS(depId, stepMap, visited)) return true
        }
        visited[id] = 2
        return false
    }

    private fun findCyclePath(steps: List<PlanStep>, stepMap: Map<UUID, PlanStep>): List<UUID> {
        // دالة مساعدة لتبسيط استخراج المسار الدائري الفعلي للعرض في الـ Logs
        return steps.map { it.id } 
    }
}
