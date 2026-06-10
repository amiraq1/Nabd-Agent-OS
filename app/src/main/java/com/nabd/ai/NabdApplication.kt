package com.nabd.ai

import android.app.Application
import com.nabd.ai.local.di.AppContainer

class NabdApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
