package com.nabd.ai.local.intelligence.graph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProjectGraphManagerTest {

    @Test
    fun `detects circular dependencies correctly`() {
        val graph = ProjectGraphManager()
        
        // A -> B -> C -> A
        graph.addEdge("A", "B", DependencyType.IMPORT)
        graph.addEdge("B", "C", DependencyType.IMPORT)
        graph.addEdge("C", "A", DependencyType.IMPORT)
        
        assertTrue(graph.hasCircularDependency("A"))
        assertTrue(graph.hasCircularDependency("B"))
        assertTrue(graph.hasCircularDependency("C"))
        
        // Add independent node D
        graph.addEdge("C", "D", DependencyType.IMPORT)
        assertFalse(graph.hasCircularDependency("D"))
    }
}
