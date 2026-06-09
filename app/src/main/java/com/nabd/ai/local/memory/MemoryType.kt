package com.nabd.ai.local.memory

/**
 * MemoryType: Categories of information stored by the Agent.
 */
enum class MemoryType {
    USER_FACT,      // e.g., "User lives in New York"
    PREFERENCE,     // e.g., "User prefers dark mode"
    TASK,           // e.g., "User wants to buy groceries"
    SUMMARY,        // e.g., "Summary of conversation #123"
    OBSERVATION,    // e.g., "Agent observed that user is frustrated"
    KNOWLEDGE       // e.g., "General knowledge learned during interaction"
}
