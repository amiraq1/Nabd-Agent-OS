package com.nabd.ai.local.agent.tools.intelligence

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.intelligence.impact.ImpactAnalyzer
import org.json.JSONObject

class ImpactAnalysisTool(
    private val impactAnalyzer: ImpactAnalyzer
) : ToolDefinition {
    override val name = "analyze_impact"
    override val description = "Predict the files and dependencies affected if a specific symbol is modified."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "symbol": { "type": "string", "description": "The exact name of the symbol to analyze." }
          },
          "required": ["symbol"]
        }
    """.trimIndent()
    override val category = ToolCategory.UTILITY
    override val riskLevel = ToolRiskLevel.SAFE

    override suspend fun execute(params: String): Result<String> {
        return try {
            val json = JSONObject(params)
            val symbol = json.getString("symbol")
            
            val report = impactAnalyzer.analyzeImpact(symbol)
            if (report.affectedFiles.isEmpty()) {
                Result.success("Modifying '$symbol' appears to have no cross-file impact.")
            } else {
                val formatted = """
                    Impact Report for: $symbol
                    Affected Files: ${report.affectedFiles.size}
                    Direct Dependents: ${report.directDependents.joinToString(", ")}
                    Indirect Dependents Count: ${report.indirectDependentsCount}
                    Potential Breakage Points: 
                    ${report.potentialBreakagePoints.joinToString("\n")}
                """.trimIndent()
                Result.success(formatted)
            }
        } catch (e: Exception) {
            Result.success("Error: ${e.message}")
        }
    }
}
