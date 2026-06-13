package com.nabd.ai.local.workspace

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files

class LocalSandboxManagerTest {

    @Test
    fun `resolvePath inside workspace is successful`() {
        val tempDir = Files.createTempDirectory("workspace_test").toFile()
        val manager = LocalSandboxManager(tempDir)
        
        val resolved = manager.resolvePath("src/main/test.kt")
        assertTrue(resolved.canonicalPath.startsWith(tempDir.canonicalPath))
    }

    @Test
    fun `resolvePath with directory traversal throws SecurityException`() {
        val tempDir = Files.createTempDirectory("workspace_test").toFile()
        val manager = LocalSandboxManager(tempDir)
        
        val exception = assertThrows<SecurityException> {
            manager.resolvePath("../../etc/passwd")
        }
        assertTrue(exception.message!!.contains("Path traversal attempt detected"))
    }

    @Test
    fun `resolvePath with absolute path outside workspace throws SecurityException`() {
        val tempDir = Files.createTempDirectory("workspace_test").toFile()
        val manager = LocalSandboxManager(tempDir)
        
        val outsideDir = Files.createTempDirectory("outside_test").toFile()
        val absolutePath = outsideDir.absolutePath
        
        val exception = assertThrows<SecurityException> {
            manager.resolvePath(absolutePath)
        }
        assertTrue(exception.message!!.contains("Access denied to path outside workspace"))
    }
}
