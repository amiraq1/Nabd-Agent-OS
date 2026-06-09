package com.nabd.ai.local.agent.tools.filesystem

import com.nabd.ai.local.workspace.WorkspaceManager
import com.nabd.ai.local.workspace.WorkspaceObserver
import com.nabd.ai.local.workspace.history.WorkspaceHistoryManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class FilesystemToolsTest {

    @Test
    fun `WriteFileTool creates file and notifies observer`() = runTest {
        val tempDir = Files.createTempDirectory("ws_test").toFile()
        val file = File(tempDir, "test.txt")
        
        val workspaceManager = mockk<WorkspaceManager>()
        every { workspaceManager.resolvePath("test.txt") } returns file
        
        val historyManager = mockk<WorkspaceHistoryManager>(relaxed = true)
        val observer = mockk<WorkspaceObserver>(relaxed = true)
        
        val tool = WriteFileTool(workspaceManager, historyManager, observer)
        
        val result = tool.execute("""{"path": "test.txt", "content": "hello"}""")
        assertTrue(result.isSuccess)
        assertEquals("hello", file.readText())
        verify { observer.notifyEvent(com.nabd.ai.local.workspace.WorkspaceEventType.CREATED, "test.txt") }
    }

    @Test
    fun `EditFileTool replaces string and creates snapshot`() = runTest {
        val tempDir = Files.createTempDirectory("ws_test").toFile()
        val file = File(tempDir, "test.txt")
        file.writeText("val x = 1")
        
        val workspaceManager = mockk<WorkspaceManager>()
        every { workspaceManager.resolvePath("test.txt") } returns file
        
        val historyManager = mockk<WorkspaceHistoryManager>(relaxed = true)
        val observer = mockk<WorkspaceObserver>(relaxed = true)
        
        val tool = EditFileTool(workspaceManager, historyManager, observer)
        
        val result = tool.execute("""{"path": "test.txt", "old_string": "1", "new_string": "2"}""")
        assertTrue(result.isSuccess)
        assertEquals("val x = 2", file.readText())
        verify { historyManager.createSnapshot("test.txt") }
        verify { observer.notifyEvent(com.nabd.ai.local.workspace.WorkspaceEventType.MODIFIED, "test.txt") }
    }

    @Test
    fun `DeleteFileTool deletes file and creates snapshot`() = runTest {
        val tempDir = Files.createTempDirectory("ws_test").toFile()
        val file = File(tempDir, "test.txt")
        file.writeText("delete me")
        
        val workspaceManager = mockk<WorkspaceManager>()
        every { workspaceManager.resolvePath("test.txt") } returns file
        
        val historyManager = mockk<WorkspaceHistoryManager>(relaxed = true)
        val observer = mockk<WorkspaceObserver>(relaxed = true)
        
        val tool = DeleteFileTool(workspaceManager, historyManager, observer)
        
        val result = tool.execute("""{"path": "test.txt"}""")
        assertTrue(result.isSuccess)
        assertFalse(file.exists())
        verify { historyManager.createSnapshot("test.txt") }
        verify { observer.notifyEvent(com.nabd.ai.local.workspace.WorkspaceEventType.DELETED, "test.txt") }
    }
}
