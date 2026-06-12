package com.nabd.ai.agora.di

import android.app.Application
import android.content.Context
import com.nabd.ai.agora.data.MemoryManager
import com.nabd.ai.agora.data.SettingsManager
import com.nabd.ai.agora.data.local.ChatDao
import com.nabd.ai.agora.data.local.ChatDatabase
import com.nabd.ai.agora.data.repository.ConversationRepository
import com.nabd.ai.agora.data.repository.MemoryRepository
import com.nabd.ai.agora.data.repository.SettingsRepository
import com.nabd.ai.agora.data.AutoBackupManager
import com.nabd.ai.agora.sandbox.SandboxManagerFactory
import com.nabd.ai.agora.viewmodel.ChatViewModel
import com.nabd.ai.agora.viewmodel.ChatViewModelFactory

/**
 * Centralized dependency container (manual DI).
 *
 * Replaces the ad-hoc dependency creation previously spread across
 * MainActivity (ChatDatabase.build, ChatViewModelFactory instantiation).
 * All shared dependencies are created once and reused.
 *
 * This is a stepping stone toward a full DI framework (Hilt/Koin);
 * for a single-module project it provides sufficient decoupling and
 * testability without annotation processing overhead.
 */
class AppContainer(private val appContext: Context) {
    private val application = appContext.applicationContext as Application

    // ── Data Layer ────────────────────────────────────────────

    val settingsManager: SettingsManager by lazy { SettingsManager(appContext) }
    val memoryManager: MemoryManager by lazy { MemoryManager(appContext) }
    val database: ChatDatabase by lazy { ChatDatabase.build(appContext) }
    val chatDao: ChatDao by lazy { database.chatDao() }

    // ── Repositories ──────────────────────────────────────────

    val conversationRepository: ConversationRepository by lazy {
        ConversationRepository(chatDao)
    }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsManager)
    }
    val memoryRepository: MemoryRepository by lazy {
        MemoryRepository(memoryManager)
    }

    // ── Sandbox (flavor-specific) ─────────────────────────────

    val sandboxManagerFactory: SandboxManagerFactory? by lazy {
        try {
            // fdroid flavor provides FdroidSandboxManagerFactory
            Class.forName("com.nabd.ai.agora.sandbox.FdroidSandboxManagerFactory")
                .getDeclaredConstructor(android.content.Context::class.java)
                .newInstance(appContext) as SandboxManagerFactory
        } catch (_: Exception) {
            // play flavor provides PlaySandboxManagerFactory
            try {
                Class.forName("com.nabd.ai.agora.sandbox.PlaySandboxManagerFactory")
                    .getDeclaredConstructor()
                    .newInstance() as SandboxManagerFactory
            } catch (_: Exception) {
                null
            }
        }
    }

    // ── Auto Backup ───────────────────────────────────────────

    val autoBackupManager: AutoBackupManager by lazy {
        AutoBackupManager(appContext, settingsManager, chatDao, memoryManager)
    }

    // ── ViewModel Factory ─────────────────────────────────────

    fun chatViewModelFactory(): ChatViewModelFactory =
        ChatViewModelFactory(application, settingsManager, chatDao, memoryManager, appContext, sandboxManagerFactory)
}
