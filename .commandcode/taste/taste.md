# Taste (Continuously Learned by [CommandCode][cmd])

[cmd]: https://commandcode.ai/

# security
- For production-critical tasks: Always audit existing implementation before making any edits. Confidence: 0.85
- Ensure environment variable propagation reaches child processes (shell, subprocess, agent execution) when configuring API keys. Confidence: 0.70
- Never expose secrets in logs, diagnostics, or output — use existence-check logging (e.g., "KEY detected") without printing values. Confidence: 0.85

# architecture
- Before deleting any module/file, perform full reference search including imports, reflection, and dynamic loading, then verify build succeeds. Confidence: 0.80
- Consolidate to a single dependency injection container as the single source of truth; migrate rather than duplicate. Confidence: 0.80

# workflow
- Structure complex migration tasks with explicit: audit requirements, deliverable format, compatibility impact, security impact, and validation results per task. Confidence: 0.70
- Preserve backward compatibility and lifecycle behavior when refactoring dependency containers. Confidence: 0.75

# kotlin
- Always trim and lowercase provider strings when doing string comparisons (e.g., `provider.lowercase().trim()`). Confidence: 0.75

