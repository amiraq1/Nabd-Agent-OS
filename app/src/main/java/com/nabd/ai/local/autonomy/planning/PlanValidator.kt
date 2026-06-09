package com.nabd.ai.local.autonomy.planning

class PlanValidator {
    fun validate(plan: ExecutionPlan): Boolean {
        if (plan.steps.isEmpty()) return false
        
        val allIds = plan.steps.map { it.id }.toSet()
        
        // Ensure no invalid dependencies
        for (step in plan.steps) {
            for (dep in step.dependencies) {
                if (!allIds.contains(dep)) {
                    return false
                }
            }
        }
        
        // Check for cycles
        if (hasCycle(plan.steps)) return false
        
        return true
    }
    
    private fun hasCycle(steps: List<PlanStep>): Boolean {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        val stepMap = steps.associateBy { it.id }
        
        fun dfs(nodeId: String): Boolean {
            if (recursionStack.contains(nodeId)) return true
            if (visited.contains(nodeId)) return false
            
            visited.add(nodeId)
            recursionStack.add(nodeId)
            
            val step = stepMap[nodeId]
            if (step != null) {
                for (dep in step.dependencies) {
                    if (dfs(dep)) return true
                }
            }
            
            recursionStack.remove(nodeId)
            return false
        }
        
        for (step in steps) {
            if (dfs(step.id)) return true
        }
        
        return false
    }
}
