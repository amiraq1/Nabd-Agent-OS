package com.nabd.ai.local.intelligence.parser

interface SemanticParser {
    val supportedExtensions: Set<String>
    
    /**
     * Parses the given source code text and returns a list of identified symbols.
     */
    fun parse(content: String, filePath: String): List<CodeSymbol>

    /**
     * Extracts imports or dependencies from the source file.
     */
    fun extractImports(content: String): List<String>
}
