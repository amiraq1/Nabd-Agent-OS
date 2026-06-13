package com.nabd.ai.local.autonomy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.nabd.ai.local.autonomy.worker.AgentWorker

/**
 * AgentBootReceiver: Handles device boot and triggers background orchestration.
 * Complies with Android 14 restrictions by delegating to WorkManager.
 */
class AgentBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val workRequest = OneTimeWorkRequestBuilder<AgentWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
