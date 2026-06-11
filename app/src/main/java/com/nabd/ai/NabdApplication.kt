package com.nabd.ai

import android.app.Application
import com.nabd.ai.local.di.AppContainer

/**
 * NabdApplication: Single Source of Truth for MTP Dependency Injection.
 */
class NabdApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
