package com.nabd.ai.local.autonomy.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.nabd.ai.local.autonomy.service.AgentForegroundService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * AgentWorker: Handles background tasks and transitions to Foreground Service if needed.
 * Android 14 compliant startup orchestration.
 */
class AgentWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Here we would check if there are pending agent goals to resume
        // For now, we start the AgentForegroundService to ensure foreground execution
        AgentForegroundService.start(applicationContext, "Resuming agent autonomy...")
        
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val id = "agent_worker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "Agent Worker", NotificationManager.IMPORTANCE_LOW)
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle("Nabd-Agent-OS")
            .setContentText("Initializing background services...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()

        return ForegroundInfo(1002, notification)
    }
}
