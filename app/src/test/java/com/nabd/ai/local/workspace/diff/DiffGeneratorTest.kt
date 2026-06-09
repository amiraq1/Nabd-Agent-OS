package com.nabd.ai.local.workspace.diff

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiffGeneratorTest {

    @Test
    fun `generateDiff produces expected unified diff format`() {
        val old = "line1\nline2\nline3"
        val new = "line1\nline2 changed\nline3"
        
        val patch = DiffGenerator.generateDiff(old, new, "test.txt")
        
        assertTrue(patch.unifiedDiff.contains("--- a/test.txt"))
        assertTrue(patch.unifiedDiff.contains("+++ b/test.txt"))
        assertTrue(patch.unifiedDiff.contains("- line2"))
        assertTrue(patch.unifiedDiff.contains("+ line2 changed"))
        assertTrue(patch.unifiedDiff.contains("  line1"))
    }
}
