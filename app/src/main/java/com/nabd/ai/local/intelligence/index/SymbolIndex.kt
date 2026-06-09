package com.nabd.ai.local.intelligence.index

import com.nabd.ai.local.intelligence.parser.CodeSymbol
import java.util.concurrent.ConcurrentHashMap

class SymbolIndex {
    // Map of SymbolName -> List of CodeSymbol instances
    private val index = ConcurrentHashMap<String, MutableList<CodeSymbol>>()
    
    // Map of FilePath -> List of SymbolNames (for easy removal)
    private val fileToSymbols = ConcurrentHashMap<String, MutableSet<String>>()

    fun addSymbols(filePath: String, symbols: List<CodeSymbol>) {
        val fileSymbols = fileToSymbols.getOrPut(filePath) { ConcurrentHashMap.newKeySet() }
        
        for (symbol in symbols) {
            val list = index.getOrPut(symbol.name) { mutableListOf() }
            // Remove old version if it exists
            list.removeIf { it.filePath == filePath && it.name == symbol.name }
            list.add(symbol)
            fileSymbols.add(symbol.name)
        }
    }

    fun removeFile(filePath: String) {
        val symbols = fileToSymbols.remove(filePath) ?: return
        for (symbolName in symbols) {
            index[symbolName]?.removeIf { it.filePath == filePath }
            if (index[symbolName]?.isEmpty() == true) {
                index.remove(symbolName)
            }
        }
    }

    fun findDefinition(symbolName: String): List<CodeSymbol> {
        return index[symbolName]?.toList() ?: emptyList()
    }

    fun getAllSymbolsInFile(filePath: String): List<CodeSymbol> {
        val symbolNames = fileToSymbols[filePath] ?: return emptyList()
        val results = mutableListOf<CodeSymbol>()
        for (name in symbolNames) {
            index[name]?.filter { it.filePath == filePath }?.let { results.addAll(it) }
        }
        return results
    }

    fun clear() {
        index.clear()
        fileToSymbols.clear()
    }
}
