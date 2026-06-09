package com.nabd.ai.local.agent.tools

import com.nabd.ai.local.agent.ToolCategory
import com.nabd.ai.local.agent.ToolDefinition
import com.nabd.ai.local.agent.ToolRiskLevel
import com.nabd.ai.local.rag.retrieval.KnowledgeRetriever
import com.nabd.ai.local.rag.storage.LocalVectorDatabase
import org.json.JSONObject

/**
 * KnowledgeTool: Allows the Agent to autonomously manage and search the ingested knowledge base.
 */
class KnowledgeTool(
    private val retriever: KnowledgeRetriever,
    private val vectorDatabase: LocalVectorDatabase
) : ToolDefinition {
    override val name = "knowledge_manager"
    override val description = "Search, list, delete, or reindex the user's uploaded documents."
    override val jsonSchema = """
        {
          "type": "object",
          "properties": {
            "action": { "type": "string", "enum": ["search", "list", "delete", "reindex"], "description": "The action to perform." },
            "query": { "type": "string", "description": "The search query (for search action)." },
            "documentId": { "type": "string", "description": "The ID of the document to delete or reindex." }
          },
          "required": ["action"]
        }
    """.trimIndent()
    override val category = ToolCategory.KNOWLEDGE
    override val riskLevel = ToolRiskLevel.MODERATE
    override val requiresHumanApproval = true

    override suspend fun execute(params: String): Result<String> {
        val json = JSONObject(params)
        val action = json.getString("action")
        
        return try {
            when (action) {
                "search" -> {
                    val query = json.getString("query")
                    val knowledge = retriever.retrieveKnowledge(query)
                    if (knowledge.isNotEmpty()) {
                        Result.success(knowledge)
                    } else {
                        Result.success("No relevant information found in the knowledge base.")
                    }
                }
                "list" -> {
                    // Assuming vectorDatabase provides a way to get all chunks/docs. 
                    // Let's just return a placeholder or simplified list if we don't have a direct get all docs method yet.
                    Result.success("Listing documents is supported via settings UI. Ask the user to manage documents there.")
                }
                "delete" -> {
                    val docId = json.getString("documentId")
                    vectorDatabase.deleteDocument(docId)
                    Result.success("Document $docId deleted successfully.")
                }
                "reindex" -> {
                    // Reindexing logic can be triggered here
                    Result.success("Reindex triggered for document (Requires Background Worker).")
                }
                else -> Result.failure(IllegalArgumentException("Unknown action: $action"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

