package com.nabd.ai.local.intelligence.graph

enum class DependencyType {
    IMPORT, USAGE, INHERITANCE
}

data class DependencyNode(
    val id: String // Usually filePath or fully qualified symbol name
)

data class DependencyEdge(
    val source: DependencyNode,
    val target: DependencyNode,
    val type: DependencyType,
    val metadata: String = ""
)
