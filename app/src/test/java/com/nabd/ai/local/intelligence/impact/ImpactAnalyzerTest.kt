package com.nabd.ai.local.intelligence.impact

import com.nabd.ai.local.intelligence.graph.DependencyType
import com.nabd.ai.local.intelligence.graph.ProjectGraphManager
import com.nabd.ai.local.intelligence.index.SymbolIndex
import com.nabd.ai.local.intelligence.parser.CodeSymbol
import com.nabd.ai.local.intelligence.parser.SymbolType
import com.nabd.ai.local.intelligence.parser.Visibility
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImpactAnalyzerTest {

    @Test
    fun `analyzes impact based on symbol definitions and graph edges`() {
        val symbolIndex = SymbolIndex()
        val graph = ProjectGraphManager()
        
        // fileA.kt defines TargetSymbol
        val symbol = CodeSymbol("TargetSymbol", SymbolType.CLASS, "fileA.kt", 1, 1, Visibility.PUBLIC)
        symbolIndex.addSymbols("fileA.kt", listOf(symbol))
        
        // fileB.kt imports/uses fileA.kt
        graph.addEdge("fileB.kt", "fileA.kt", DependencyType.USAGE)
        
        // fileC.kt imports fileB.kt
        graph.addEdge("fileC.kt", "fileB.kt", DependencyType.USAGE)
        
        val analyzer = ImpactAnalyzer(symbolIndex, graph)
        val report = analyzer.analyzeImpact("TargetSymbol")
        
        assertEquals("TargetSymbol", report.targetSymbol)
        assertTrue(report.affectedFiles.contains("fileA.kt"))
        assertTrue(report.affectedFiles.contains("fileB.kt"))
        assertTrue(report.directDependents.contains("fileB.kt"))
        // fileC is an indirect dependent of fileA through fileB. 
        // Our simple analyzer checks 1 level deep for count. fileB has 1 incoming (fileC).
        assertEquals(1, report.indirectDependentsCount)
    }
}
