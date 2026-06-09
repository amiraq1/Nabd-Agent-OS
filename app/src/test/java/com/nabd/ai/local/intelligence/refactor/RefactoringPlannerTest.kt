package com.nabd.ai.local.intelligence.refactor

import com.nabd.ai.local.intelligence.index.SymbolIndex
import com.nabd.ai.local.intelligence.parser.CodeSymbol
import com.nabd.ai.local.intelligence.parser.SymbolType
import com.nabd.ai.local.intelligence.parser.Visibility
import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.search.CodeSearchEngine
import com.nabd.ai.local.workspace.search.SearchResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class RefactoringPlannerTest {

    @Test
    fun `plans rename across multiple files correctly`() = runTest {
        val symbolIndex = SymbolIndex()
        val workspaceManager = mockk<WorkspaceManager>()
        val searchEngine = mockk<CodeSearchEngine>()
        
        val tempDir = Files.createTempDirectory("refactor_test").toFile()
        val file1 = File(tempDir, "File1.kt").apply { writeText("class OldName { }") }
        val file2 = File(tempDir, "File2.kt").apply { writeText("val obj = OldName()") }
        
        every { workspaceManager.resolvePath("File1.kt") } returns file1
        every { workspaceManager.resolvePath("File2.kt") } returns file2
        
        symbolIndex.addSymbols("File1.kt", listOf(
            CodeSymbol("OldName", SymbolType.CLASS, "File1.kt", 1, 1, Visibility.PUBLIC)
        ))
        
        coEvery { searchEngine.searchContent("\\bOldName\\b") } returns listOf(
            SearchResult("File2.kt", 1, "val obj = OldName()")
        )
        
        val planner = RefactoringPlanner(workspaceManager, searchEngine, symbolIndex)
        val transaction = planner.planRename("OldName", "NewName")
        
        assertEquals(2, transaction.affectedFiles.size)
        assertTrue(transaction.affectedFiles.contains("File1.kt"))
        assertTrue(transaction.affectedFiles.contains("File2.kt"))
        
        val patch1 = transaction.patches.find { it.relativePath == "File1.kt" }
        assertNotNull(patch1)
        assertTrue(patch1!!.unifiedDiff.contains("+ class NewName"))
        
        val patch2 = transaction.patches.find { it.relativePath == "File2.kt" }
        assertNotNull(patch2)
        assertTrue(patch2!!.unifiedDiff.contains("+ val obj = NewName()"))
    }
}
