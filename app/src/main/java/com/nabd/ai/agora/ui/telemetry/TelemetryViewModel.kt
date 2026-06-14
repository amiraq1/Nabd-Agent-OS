package com.nabd.ai.agora.ui.telemetry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nabd.ai.agora.utils.RamSnapshot
import com.nabd.ai.agora.utils.TelemetryMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class TokenSnapshot(val used: Int, val total: Int) {
    val percent: Int get() = (used.toFloat() / total * 100).roundToInt().coerceIn(0, 100)
    val label: String get() = "${used.formatWithCommas()} / ${total.formatWithCommas()} tokens"

    private fun Int.formatWithCommas(): String {
        return "%,d".format(this)
    }
}

class TelemetryViewModel(application: Application) : AndroidViewModel(application) {

    val ramMetrics: StateFlow<RamSnapshot> = TelemetryMonitor.memoryTickerFlow(application)
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = TelemetryMonitor.getMemoryMetrics(application)
        )

    // Phase 8 placeholder — do NOT use DataStore here
    val contextTokens: StateFlow<TokenSnapshot> =
        MutableStateFlow(TokenSnapshot(used = 1_240, total = 8_192))
            .asStateFlow()

    val inferenceSpeed: StateFlow<Float> =
        MutableStateFlow(0f).asStateFlow()
        // Phase 8: wire real inference callback here

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TelemetryViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return TelemetryViewModel(app) as T
        }
    }
}
