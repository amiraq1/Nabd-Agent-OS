package com.nabd.ai.local.rag.retrieval

class RetrievalEvaluator {
    fun calculatePrecisionAtK(retrievedIds: List<String>, relevantIds: Set<String>, k: Int): Double {
        if (k <= 0 || retrievedIds.isEmpty()) return 0.0
        val topK = retrievedIds.take(k)
        val relevantRetrieved = topK.count { it in relevantIds }
        return relevantRetrieved.toDouble() / k
    }

    fun calculateRecallAtK(retrievedIds: List<String>, relevantIds: Set<String>, k: Int): Double {
        if (relevantIds.isEmpty()) return 1.0
        if (k <= 0 || retrievedIds.isEmpty()) return 0.0
        val topK = retrievedIds.take(k)
        val relevantRetrieved = topK.count { it in relevantIds }
        return relevantRetrieved.toDouble() / relevantIds.size
    }

    fun calculateMRR(retrievedIdsList: List<List<String>>, relevantIdsList: List<Set<String>>): Double {
        if (retrievedIdsList.isEmpty() || retrievedIdsList.size != relevantIdsList.size) return 0.0
        var sumRR = 0.0
        for (i in retrievedIdsList.indices) {
            val retrieved = retrievedIdsList[i]
            val relevant = relevantIdsList[i]
            val firstRelevantRank = retrieved.indexOfFirst { it in relevant }
            if (firstRelevantRank != -1) {
                sumRR += 1.0 / (firstRelevantRank + 1)
            }
        }
        return sumRR / retrievedIdsList.size
    }
}
