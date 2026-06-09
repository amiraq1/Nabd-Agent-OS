package com.nabd.ai.local.core.native

import java.util.concurrent.atomic.AtomicLong

object NativeMemoryTracker {
    private val allocatedBytes = AtomicLong(0)
    private val activeHandles = mutableSetOf<Long>()

    fun recordAllocation(handle: Long, bytes: Long) {
        activeHandles.add(handle)
        allocatedBytes.addAndGet(bytes)
    }

    fun recordDeallocation(handle: Long, bytes: Long) {
        if (activeHandles.remove(handle)) {
            allocatedBytes.addAndGet(-bytes)
        }
    }

    fun getTotalAllocatedBytes(): Long = allocatedBytes.get()
    
    fun getActiveHandlesCount(): Int = activeHandles.size
}
