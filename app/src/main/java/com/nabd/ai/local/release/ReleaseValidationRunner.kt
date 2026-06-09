package com.nabd.ai.local.release

import android.content.Context
import com.nabd.ai.local.core.health.StartupHealthCheck
import com.nabd.ai.local.core.native.NativeLeakDetector
import com.nabd.ai.local.core.diagnostics.ThermalManager
import com.nabd.ai.local.models.ModelCompatibilityChecker
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ValidationResult(
    val module: String,
    val success: Boolean,
    val message: String
)

class ReleaseValidationRunner(private val context: Context) {
    
    suspend fun runFullValidation(modelFile: File?): List<ValidationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ValidationResult>()
        
        // 1. Health Checks
        val healthCheck = StartupHealthCheck(context)
        val report = healthCheck.runChecks()
        results.add(ValidationResult("HealthCheck", report.isHealthy, "Issues: ${report.issues.joinToString()}"))

        // 2. Thermal Checks
        val thermalManager = ThermalManager(context)
        val isThrottling = thermalManager.isThermalThrottlingRequired()
        results.add(ValidationResult("ThermalState", !isThrottling, if (isThrottling) "Severe throttling detected" else "Thermal state nominal"))

        // 3. Model Compatibility (if model provided)
        if (modelFile != null && modelFile.exists()) {
            val checker = ModelCompatibilityChecker()
            val compReport = checker.checkCompatibility(modelFile)
            results.add(ValidationResult("ModelCompatibility", compReport.isCompatible, compReport.reason ?: "Model is compatible"))
        }

        // 4. Native Leak Baseline
        val leakDetector = NativeLeakDetector()
        val leakBaseline = leakDetector.verifyBaseline()
        results.add(ValidationResult("NativeMemory", leakBaseline, if (leakBaseline) "Baseline clean" else "Leaks detected at baseline"))

        return@withContext results
    }
}
