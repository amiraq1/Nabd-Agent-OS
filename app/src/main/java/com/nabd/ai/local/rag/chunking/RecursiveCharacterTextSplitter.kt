package com.nabd.ai.local.rag.chunking

class RecursiveCharacterTextSplitter(
    private val chunkSize: Int = 512,
    private val chunkOverlap: Int = 50
) : TextSplitter {

    override fun splitText(text: String): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        
        while (start < text.length) {
            var end = start + chunkSize
            
            if (end >= text.length) {
                val chunk = text.substring(start).trim()
                if (chunk.isNotEmpty()) {
                    chunks.add(chunk)
                }
                break
            }
            
            // Backtrack to find a natural break point (e.g., newline or space)
            var breakPoint = end
            val minBreakPoint = maxOf(start + (chunkSize / 2), end - 100)
            
            for (i in end downTo minBreakPoint) {
                if (text[i] == '\n' || text[i] == ' ') {
                    breakPoint = i
                    break
                }
            }
            
            val chunk = text.substring(start, breakPoint).trim()
            if (chunk.isNotEmpty()) {
                chunks.add(chunk)
            }
            
            start = breakPoint - chunkOverlap
            // Ensure we move forward to prevent infinite loops
            if (start <= breakPoint - chunk.length) {
                start = breakPoint // Force forward progress if overlap is too big
            }
        }
        
        return chunks
    }
}
