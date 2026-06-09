package com.nabd.ai.local.intelligence.graph

import java.util.concurrent.ConcurrentHashMap

class ProjectGraphManager {
    private val nodes = ConcurrentHashMap<String, DependencyNode>()
    // sourceId -> List of outbound edges
    private val outgoingEdges = ConcurrentHashMap<String, MutableList<DependencyEdge>>()
    // targetId -> List of inbound edges
    private val incomingEdges = ConcurrentHashMap<String, MutableList<DependencyEdge>>()

    fun addNode(id: String): DependencyNode {
        return nodes.getOrPut(id) { DependencyNode(id) }
    }

    fun addEdge(sourceId: String, targetId: String, type: DependencyType, metadata: String = "") {
        val source = addNode(sourceId)
        val target = addNode(targetId)
        
        val edge = DependencyEdge(source, target, type, metadata)
        
        outgoingEdges.getOrPut(sourceId) { mutableListOf() }.add(edge)
        incomingEdges.getOrPut(targetId) { mutableListOf() }.add(edge)
    }

    fun removeEdgesFrom(sourceId: String) {
        val edgesToRemove = outgoingEdges.remove(sourceId) ?: return
        for (edge in edgesToRemove) {
            incomingEdges[edge.target.id]?.remove(edge)
        }
    }

    fun getOutgoingDependencies(nodeId: String): List<DependencyEdge> {
        return outgoingEdges[nodeId]?.toList() ?: emptyList()
    }

    fun getIncomingDependencies(nodeId: String): List<DependencyEdge> {
        return incomingEdges[nodeId]?.toList() ?: emptyList()
    }

    fun hasCircularDependency(startNodeId: String): Boolean {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        fun dfs(currentId: String): Boolean {
            if (recursionStack.contains(currentId)) return true
            if (visited.contains(currentId)) return false

            visited.add(currentId)
            recursionStack.add(currentId)

            val edges = outgoingEdges[currentId] ?: emptyList()
            for (edge in edges) {
                if (dfs(edge.target.id)) return true
            }

            recursionStack.remove(currentId)
            return false
        }

        return dfs(startNodeId)
    }

    fun clear() {
        nodes.clear()
        outgoingEdges.clear()
        incomingEdges.clear()
    }
}
