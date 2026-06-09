package com.nabd.ai.local.rag.chunking

interface TextSplitter {
    fun splitText(text: String): List<String>
}
