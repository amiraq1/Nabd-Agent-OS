package com.nabd.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nabd.ai.ui.theme.NabdTheme
import com.nabd.ai.ui.AvantGardeAppLayout

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Retrieve the AppContainer from the custom Application class
        val appContainer = (application as NabdApplication).container

        // Execute Identity Migration Layers safely before any Room DB or File accesses
        com.nabd.ai.local.core.migration.DatabaseMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.WorkspaceMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.ModelMigrationManager(applicationContext).migrate()
        com.nabd.ai.local.core.migration.SettingsMigrationManager(applicationContext).migrate()

        setContent {
            NabdTheme {
                // The new Avant-Garde UI layout that depends on AppContainer instead of raw managers
                AvantGardeAppLayout(appContainer)
            }
        }
    }
}
