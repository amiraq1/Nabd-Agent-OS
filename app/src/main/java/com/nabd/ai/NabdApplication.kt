package com.nabd.ai

import android.app.Application
import com.nabd.ai.local.mtp_engine.di.NabdContainer

/**
 * NabdApplication: Single Source of Truth for MTP Dependency Injection.
 */
class NabdApplication : Application() {
    lateinit var container: NabdContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = NabdContainer(this)
    }
}
