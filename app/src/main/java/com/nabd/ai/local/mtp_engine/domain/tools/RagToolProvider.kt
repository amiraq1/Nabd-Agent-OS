package com.nabd.ai.local.mtp_engine.domain.tools

import com.nabd.ai.local.mtp_engine.domain.rag.RagManager

/**
 * RagToolProvider: Active Semantic Retrieval.
 * Intentional Minimalism - Exposes RAG capabilities directly to the agent.
 */
class RagToolProvider(
    private val ragManager: RagManager
) : ToolProvider {
    
    override val name: String = "search_memory"

    override fun canHandle(toolName: String): Boolean = toolName == name

    override suspend fun execute(arguments: String): ToolResult {
        return try {
            val query = extractQuery(arguments)
            val results = ragManager.searchMemory(query)
            
            if (results.isEmpty()) {
                ToolResult.Success("MEMORY_FAULT :: NO_RELEVANT_CONTEXT_FOUND")
            } else {
                // تنسيق تكتيكي صارم يسهل على النموذج الناتيف فهمه
                val formattedContext = results.joinToString("\n---\n") { 
                    "[${it.participant}] :: ${it.text}" 
                }
                ToolResult.Success(formattedContext)
            }
        } catch (e: Exception) {
            ToolResult.Failure("RAG_EXECUTION_FAULT :: ${e.message}")
        }
    }

    private fun extractQuery(arguments: String): String {
        // محلل O(1) سريع لاستخراج قيمة "query" من الـ JSON المتدفق
        return arguments.substringAfter("\"query\":").substringBefore("}").trim(' ', '"', '\n')
    }
}
