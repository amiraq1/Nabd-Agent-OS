package com.nabd.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nabd.ai.ui.theme.NabdTheme


/**
 * MainActivity: Edge-to-Edge Minimalism - Bootstrapping the Nabd OS.
 * Serves as the primary entry point for the unified MTP experience.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Remove visual system boundaries to fully integrate the UI
        enableEdgeToEdge() 
        
        // Retrieve the AppContainer from the custom Application class
        val appContainer = (application as NabdApplication).container

        setContent {
            NabdTheme {
                // The new Avant-Garde UI layout that depends on AppContainer instead of raw managers
                com.nabd.ai.ui.AvantGardeAppLayout(appContainer)
            }
        }
    }
}
