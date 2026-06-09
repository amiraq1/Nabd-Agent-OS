package com.nabd.ai.local.models

import java.io.File
import java.io.RandomAccessFile

data class CompatibilityReport(
    val isCompatible: Boolean,
    val reason: String? = null
)

class ModelCompatibilityChecker {
    fun checkCompatibility(modelFile: File): CompatibilityReport {
        if (!modelFile.exists()) return CompatibilityReport(false, "File does not exist")
        
        // Basic GGUF magic number check (0x46554747 or 'GGUF')
        try {
            RandomAccessFile(modelFile, "r").use { raf ->
                val magic = ByteArray(4)
                raf.read(magic)
                val magicStr = String(magic)
                if (magicStr != "GGUF") {
                    return CompatibilityReport(false, "Invalid file format. Only GGUF is supported. Found: $magicStr")
                }
                
                val version = Integer.reverseBytes(raf.readInt()) // GGUF is little endian
                if (version < 2 || version > 3) {
                     // Note: GGUF v2 and v3 are standard.
                     return CompatibilityReport(false, "Unsupported GGUF version: $version")
                }
            }
        } catch (e: Exception) {
            return CompatibilityReport(false, "Failed to read model file: ${e.message}")
        }
        
        return CompatibilityReport(true)
    }
}
