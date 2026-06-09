package com.nabd.ai.local.intelligence.parser

import java.util.regex.Pattern

class JavaSymbolExtractor : SemanticParser {
    override val supportedExtensions = setOf("java")

    private val classRegex = Pattern.compile("(?<vis>public|private|protected)?\\s*(?:static|final|abstract)?\\s*(?:class|interface|enum)\\s+(?<name>[a-zA-Z0-9_]+)")
    private val methodRegex = Pattern.compile("(?<vis>public|private|protected)?\\s*(?:static|final|abstract|synchronized)?\\s*(?:<[^>]+>\\s+)?[a-zA-Z0-9_<>\\[\\]]+\\s+(?<name>[a-zA-Z0-9_]+)\\s*\\(")
    private val importRegex = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([a-zA-Z0-9_.]+)(?:\\.\\*)?\\s*;\\s*$", Pattern.MULTILINE)

    override fun parse(content: String, filePath: String): List<CodeSymbol> {
        val symbols = mutableListOf<CodeSymbol>()
        val lines = content.lines()

        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            
            val classMatcher = classRegex.matcher(line)
            if (classMatcher.find()) {
                val visStr = classMatcher.group("vis")
                val name = classMatcher.group("name") ?: continue
                val type = if (line.contains("interface ")) SymbolType.INTERFACE else SymbolType.CLASS
                symbols.add(CodeSymbol(name, type, filePath, lineNumber, lineNumber, parseVisibility(visStr), line.trim()))
                continue
            }

            val methodMatcher = methodRegex.matcher(line)
            // Filtering out simple keyword constructs like 'if (' or 'while ('
            if (methodMatcher.find() && !line.matches(".*\\b(if|while|for|switch|catch)\\b.*".toRegex())) {
                val visStr = methodMatcher.group("vis")
                val name = methodMatcher.group("name") ?: continue
                symbols.add(CodeSymbol(name, SymbolType.FUNCTION, filePath, lineNumber, lineNumber, parseVisibility(visStr), line.trim()))
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
            else -> Visibility.UNKNOWN // Package private
        }
    }
}
