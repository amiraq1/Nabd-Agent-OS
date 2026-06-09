package com.nabd.ai.local.intelligence.impact

import com.nabd.ai.local.intelligence.graph.ProjectGraphManager
import com.nabd.ai.local.intelligence.index.SymbolIndex

data class ImpactReport(
    val targetSymbol: String,
    val directDependents: List<String>,
    val indirectDependentsCount: Int,
    val affectedFiles: Set<String>,
    val potentialBreakagePoints: List<String>
)

class ImpactAnalyzer(
    private val symbolIndex: SymbolIndex,
    private val graphManager: ProjectGraphManager
) {
    fun analyzeImpact(symbolName: String): ImpactReport {
        // Assuming the graph manager uses symbol names or file paths as node IDs.
        // For a full system, you might have nodes for files AND symbols.
        // Let's assume graph tracks file dependencies for simplicity in this milestone,
        // but we'll try to find incoming edges to the file where the symbol lives.
        
        val definitions = symbolIndex.findDefinition(symbolName)
        if (definitions.isEmpty()) {
            return ImpactReport(symbolName, emptyList(), 0, emptySet(), emptyList())
        }
        
        val targetFiles = definitions.map { it.filePath }.toSet()
        val affectedFiles = mutableSetOf<String>()
        val directDependents = mutableListOf<String>()
        var indirectDependentsCount = 0
        
        // Find all files that depend on the files defining this symbol
        for (targetFile in targetFiles) {
            affectedFiles.add(targetFile)
            val incoming = graphManager.getIncomingDependencies(targetFile)
            
            for (edge in incoming) {
                val dependentFile = edge.source.id
                if (!affectedFiles.contains(dependentFile)) {
                    affectedFiles.add(dependentFile)
                    directDependents.add(dependentFile)
                    
                    // Count indirect
                    val secondaryIncoming = graphManager.getIncomingDependencies(dependentFile)
                    indirectDependentsCount += secondaryIncoming.size
                }
            }
        }
        
        val breakages = directDependents.map { "File '$it' imports/uses something from the target files." }
        
        return ImpactReport(
            targetSymbol = symbolName,
            directDependents = directDependents,
            indirectDependentsCount = indirectDependentsCount,
            affectedFiles = affectedFiles,
            potentialBreakagePoints = breakages
        )
    }
}
