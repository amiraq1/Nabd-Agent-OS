package com.nabd.ai.local.workspace.diff

import java.io.File

object DiffGenerator {
    fun generateDiff(oldContent: String, newContent: String, relativePath: String): FilePatch {
        // A very simplified line-based diff generator for the agent's preview
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()
        
        val diffBuilder = java.lang.StringBuilder()
        diffBuilder.append("--- a/$relativePath\n")
        diffBuilder.append("+++ b/$relativePath\n")
        
        // This is a naive diff that just shows added/removed for different lengths or line-by-line mismatch.
        // A full Myers diff algorithm is ideal but this serves the sandbox preview requirement.
        val maxLines = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)
            
            if (oldLine != newLine) {
                if (oldLine != null) diffBuilder.append("- $oldLine\n")
                if (newLine != null) diffBuilder.append("+ $newLine\n")
            } else if (oldLine != null) {
                diffBuilder.append("  $oldLine\n")
            }
        }
        
        return FilePatch(relativePath, diffBuilder.toString().trimEnd())
    }

    fun generateDiffForNewFile(newContent: String, relativePath: String): FilePatch {
        return generateDiff("", newContent, relativePath)
    }
}
