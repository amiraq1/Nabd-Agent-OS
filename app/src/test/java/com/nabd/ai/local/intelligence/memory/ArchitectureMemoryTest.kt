package com.nabd.ai.local.intelligence.memory

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class ArchitectureMemoryTest {

    @Test
    fun `saves and loads snapshot correctly`() {
        val tempDir = Files.createTempDirectory("arch_test").toFile()
        val file = File(tempDir, "arch.json")
        val memory = ArchitectureMemory(file)
        
        val snapshot = ArchitectureSnapshot(
            entryPoints = listOf("Main.kt", "App.kt"),
            coreModules = listOf("rag", "agent", "intelligence"),
            description = "A complex AI system.",
            timestamp = 123456789L
        )
        
        memory.save(snapshot)
        
        val loaded = memory.load()
        assertNotNull(loaded)
        assertEquals(2, loaded?.entryPoints?.size)
        assertEquals("Main.kt", loaded?.entryPoints?.get(0))
        assertEquals("A complex AI system.", loaded?.description)
        assertEquals(123456789L, loaded?.timestamp)
    }
}
