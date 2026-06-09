package com.nabd.ai.local.embedding

/**
 * EmbeddingDiagnostics: Ensures the integrity of the vector store and the provider.
 */
class EmbeddingDiagnostics(
    private val provider: EmbeddingProvider,
    private val vectorStore: VectorStore
) {

    suspend fun validateDimensions(): Boolean {
        return try {
            val testEmbed = provider.embed("test")
            testEmbed.size == provider.dimensions
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validateSimilaritySanity(): Boolean {
        return try {
            val embed1 = provider.embed("hello world")
            val embed2 = provider.embed("hello world")
            val embed3 = provider.embed("completely unrelated text about something else")
            
            val simIdentical = Similarity.cosineSimilarity(embed1, embed2)
            val simDifferent = Similarity.cosineSimilarity(embed1, embed3)
            
            simIdentical > 0.9f && simIdentical > simDifferent
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyStoreIntegrity(): Boolean {
        val allEmbeddings = vectorStore.getAllEmbeddings()
        return allEmbeddings.all { it.dimensions == provider.dimensions && it.embedding.size == provider.dimensions }
    }
}
