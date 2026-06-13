package com.nabd.ai.local.autonomy.safety

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class ToolSandboxTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var sandbox: ToolSandbox

    @Before
    fun setUp() {
        sandbox = ToolSandbox(tempFolder.root)
    }

    // ── Command Validation ──────────────────────────────────

    @Test
    fun `validateCommand blocks rm -rf`() {
        assertFalse(sandbox.validateCommand("rm -rf /"))
    }

    @Test
    fun `validateCommand blocks chmod 777`() {
        assertFalse(sandbox.validateCommand("chmod 777 /etc/passwd"))
    }

    @Test
    fun `validateCommand blocks killall`() {
        assertFalse(sandbox.validateCommand("killall nginx"))
    }

    @Test
    fun `validateCommand blocks dd if`() {
        assertFalse(sandbox.validateCommand("dd if=/dev/zero of=/dev/sda"))
    }

    @Test
    fun `validateCommand blocks mkfs`() {
        assertFalse(sandbox.validateCommand("mkfs.ext4 /dev/sda1"))
    }

    @Test
    fun `validateCommand allows safe commands`() {
        assertTrue(sandbox.validateCommand("ls -la"))
        assertTrue(sandbox.validateCommand("cat README.md"))
        assertTrue(sandbox.validateCommand("echo hello"))
        assertTrue(sandbox.validateCommand("grep -r pattern ."))
    }

    @Test
    fun `validateCommand is case insensitive`() {
        assertFalse(sandbox.validateCommand("RM -RF /"))
        assertFalse(sandbox.validateCommand("Chmod 777 /tmp"))
    }

    // ── Path Validation ─────────────────────────────────────

    @Test
    fun `validatePath allows paths within workspace`() {
        val file = tempFolder.newFile("test.txt")
        assertTrue(sandbox.validatePath(file.absolutePath))
    }

    @Test
    fun `validatePath allows relative paths within workspace`() {
        tempFolder.newFile("subdir.txt")
        assertTrue(sandbox.validatePath("subdir.txt"))
    }

    @Test
    fun `validatePath blocks paths outside workspace`() {
        assertFalse(sandbox.validatePath("/etc/passwd"))
        assertFalse(sandbox.validatePath("/root/.ssh/id_rsa"))
    }

    @Test
    fun `validatePath blocks directory traversal attacks`() {
        assertFalse(sandbox.validatePath("../../etc/passwd"))
    }

    // ── Custom Blocked Commands ─────────────────────────────

    @Test
    fun `custom blocked commands are enforced`() {
        val customSandbox = ToolSandbox(
            tempFolder.root,
            blockedCommands = listOf("curl", "wget", "nc")
        )
        assertFalse(customSandbox.validateCommand("curl https://evil.com"))
        assertFalse(customSandbox.validateCommand("wget http://malware.zip"))
        assertTrue(customSandbox.validateCommand("ls -la")) // Not blocked
    }
}
