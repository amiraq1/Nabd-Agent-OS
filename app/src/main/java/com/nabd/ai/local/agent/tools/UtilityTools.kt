package com.nabd.ai.local.agent.tools

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeTool : ToolDefinition {
    override val name = "get_current_time"
    override val description = "Returns the current system time."
    override val jsonSchema = "{}" // No parameters
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return Result.success(sdf.format(Date()))
    }
}

class CalculatorTool : ToolDefinition {
    override val name = "calculator"
    override val description = "Performs simple arithmetic operations (+, -, *, /)."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "expression": { "type": "string", "description": "The math expression to evaluate." }
          },
          "required": ["expression"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        // Mock evaluation for validation
        return Result.success("Result: 42 (Expression was: $params)")
    }
}
