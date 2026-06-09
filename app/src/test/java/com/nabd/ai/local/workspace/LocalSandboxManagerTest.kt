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
        
        val exception = assertThrows<SecurityException> {
            // Passing an absolute path usually gets interpreted relative to the root if not careful,
            // but File(root, absolute) behaves weirdly. Let's test standard traversal.
            manager.resolvePath("/etc/passwd")
        }
        assertTrue(exception.message!!.contains("Path traversal attempt detected"))
    }
}
