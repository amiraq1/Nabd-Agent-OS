package com.nabd.ai.local.rag.retrieval

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RetrievalEvaluatorTest {

    private val evaluator = RetrievalEvaluator()

    @Test
    fun `calculates precision at K correctly`() {
        val retrieved = listOf("A", "B", "C", "D", "E")
        val relevant = setOf("A", "C", "Z")
        
        val pAt3 = evaluator.calculatePrecisionAtK(retrieved, relevant, 3)
        // Top 3 are A, B, C. Relevant: A, C. -> 2/3
        assertEquals(2.0 / 3.0, pAt3, 0.001)
    }

    @Test
    fun `calculates recall at K correctly`() {
        val retrieved = listOf("A", "B", "C", "D", "E")
        val relevant = setOf("A", "C", "Z")
        
        val rAt3 = evaluator.calculateRecallAtK(retrieved, relevant, 3)
        // Top 3 are A, B, C. Relevant retrieved: A, C. Total relevant: 3. -> 2/3
        assertEquals(2.0 / 3.0, rAt3, 0.001)
    }

    @Test
    fun `calculates MRR correctly`() {
        val retrievedList = listOf(
            listOf("A", "B", "C"), // Relevant "B" is at index 1 (rank 2) -> RR 1/2
            listOf("X", "Y", "Z")  // Relevant "X" is at index 0 (rank 1) -> RR 1/1
        )
        val relevantList = listOf(
            setOf("B"),
            setOf("X")
        )
        
        val mrr = evaluator.calculateMRR(retrievedList, relevantList)
        assertEquals((0.5 + 1.0) / 2.0, mrr, 0.001)
    }
}
