package com.nabd.ai.local.intelligence.parser

import java.util.regex.Pattern

/**
 * A highly simplified regex-based parser for Kotlin files.
 * Real production ASTs (like TreeSitter) are too heavy to spin up locally without native bindings,
 * so we use a robust heuristic extractor for symbols.
 */
class KotlinSymbolExtractor : SemanticParser {
    override val supportedExtensions = setOf("kt", "kts")

    // Very simplified regexes for structural parsing
    private val classRegex = Pattern.compile("(?<vis>public|private|protected|internal)?\\s*(?:data|sealed|open|abstract|annotation|value)?\\s*(?:class|interface|object)\\s+(?<name>[a-zA-Z0-9_]+)")
    private val funRegex = Pattern.compile("(?<vis>public|private|protected|internal)?\\s*(?:override|open|abstract|suspend|inline|tailrec|operator|infix)?\\s*fun\\s+(?:<[^>]+>\\s+)?(?:[a-zA-Z0-9_.]+\\.)?(?<name>[a-zA-Z0-9_]+)\\s*\\(")
    private val valRegex = Pattern.compile("(?<vis>public|private|protected|internal)?\\s*(?:override|open|abstract|lateinit|const)?\\s*(?:val|var)\\s+(?<name>[a-zA-Z0-9_]+)")
    private val importRegex = Pattern.compile("^\\s*import\\s+([a-zA-Z0-9_.]+)(?:\\.\\*)?\\s*$", Pattern.MULTILINE)

    override fun parse(content: String, filePath: String): List<CodeSymbol> {
        val symbols = mutableListOf<CodeSymbol>()
        val lines = content.lines()

        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            
            // Class/Object/Interface
            val classMatcher = classRegex.matcher(line)
            if (classMatcher.find()) {
                val visStr = classMatcher.group("vis")
                val name = classMatcher.group("name") ?: continue
                val type = if (line.contains("interface ")) SymbolType.INTERFACE else if (line.contains("object ")) SymbolType.OBJECT else SymbolType.CLASS
                symbols.add(CodeSymbol(name, type, filePath, lineNumber, lineNumber, parseVisibility(visStr), line.trim()))
                continue
            }

            // Function
            val funMatcher = funRegex.matcher(line)
            if (funMatcher.find()) {
                val visStr = funMatcher.group("vis")
                val name = funMatcher.group("name") ?: continue
                symbols.add(CodeSymbol(name, SymbolType.FUNCTION, filePath, lineNumber, lineNumber, parseVisibility(visStr), line.trim()))
                continue
            }

            // Property/Constant
            val valMatcher = valRegex.matcher(line)
            if (valMatcher.find()) {
                val visStr = valMatcher.group("vis")
                val name = valMatcher.group("name") ?: continue
                val type = if (line.contains("const ")) SymbolType.CONSTANT else SymbolType.PROPERTY
                symbols.add(CodeSymbol(name, type, filePath, lineNumber, lineNumber, parseVisibility(visStr), line.trim()))
            }
        }

        return symbols
    }

    override fun extractImports(content: String): List<String> {
        val imports = mutableListOf<String>()
        val matcher = importRegex.matcher(content)
        while (matcher.find()) {
            matcher.group(1)?.let { imports.add(it) }
        }
        return imports
    }

    private fun parseVisibility(vis: String?): Visibility {
        return when (vis) {
            "public" -> Visibility.PUBLIC
            "private" -> Visibility.PRIVATE
            "protected" -> Visibility.PROTECTED
            "internal" -> Visibility.INTERNAL
            else -> Visibility.PUBLIC // Default in Kotlin
        }
    }
}
