package com.nabd.ai.local.intelligence.parser

enum class SymbolType {
    CLASS, INTERFACE, OBJECT, FUNCTION, CONSTRUCTOR, PROPERTY, CONSTANT, VARIABLE
}

enum class Visibility {
    PUBLIC, PRIVATE, PROTECTED, INTERNAL, UNKNOWN
}

data class CodeSymbol(
    val name: String,
    val type: SymbolType,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val visibility: Visibility,
    val snippet: String = ""
)
