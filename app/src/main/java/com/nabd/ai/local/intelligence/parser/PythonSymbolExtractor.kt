package com.nabd.ai.local.intelligence.parser

import java.util.regex.Pattern

class PythonSymbolExtractor : SemanticParser {
    override val supportedExtensions = setOf("py")

    private val classRegex = Pattern.compile("^\\s*class\\s+(?<name>[a-zA-Z0-9_]+)(?:\\([^)]*\\))?:")
    private val defRegex = Pattern.compile("^\\s*(?:async\\s+)?def\\s+(?<name>[a-zA-Z0-9_]+)\\s*\\(")
    private val importRegex = Pattern.compile("^\\s*(?:import\\s+([a-zA-Z0-9_.]+)|from\\s+([a-zA-Z0-9_.]+)\\s+import)", Pattern.MULTILINE)

    override fun parse(content: String, filePath: String): List<CodeSymbol> {
        val symbols = mutableListOf<CodeSymbol>()
        val lines = content.lines()

        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            
            val classMatcher = classRegex.matcher(line)
            if (classMatcher.find()) {
                val name = classMatcher.group("name") ?: continue
                symbols.add(CodeSymbol(name, SymbolType.CLASS, filePath, lineNumber, lineNumber, Visibility.PUBLIC, line.trim()))
                continue
            }

            val defMatcher = defRegex.matcher(line)
            if (defMatcher.find()) {
                val name = defMatcher.group("name") ?: continue
                val vis = if (name.startsWith("__") && !name.endsWith("__")) Visibility.PRIVATE else if (name.startsWith("_")) Visibility.PROTECTED else Visibility.PUBLIC
                symbols.add(CodeSymbol(name, SymbolType.FUNCTION, filePath, lineNumber, lineNumber, vis, line.trim()))
            }
        }
        return symbols
    }

    override fun extractImports(content: String): List<String> {
        val imports = mutableListOf<String>()
        val matcher = importRegex.matcher(content)
        while (matcher.find()) {
            val imp1 = matcher.group(1)
            val imp2 = matcher.group(2)
            if (imp1 != null) imports.add(imp1)
            if (imp2 != null) imports.add(imp2)
        }
        return imports
    }
}
