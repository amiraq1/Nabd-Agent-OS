package com.nabd.ai.local.core.native

class NativeLeakDetector {
    fun verifyBaseline(): Boolean {
        val bytes = NativeMemoryTracker.getTotalAllocatedBytes()
        val handles = NativeMemoryTracker.getActiveHandlesCount()
        
        if (bytes > 0 || handles > 0) {
            println("NATIVE LEAK DETECTED: $bytes bytes remaining across $handles active handles.")
            return false
        }
        return true
    }
}
