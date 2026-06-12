package com.nabd.ai.agora.service

object AppForegroundTracker {
    @Volatile
    var isInForeground: Boolean = false
}
