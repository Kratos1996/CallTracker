package com.ishant.callsoftware.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ishant.callsoftware.utils.keepAliveService

class NotificationServiceRestartReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(action?.equals(Intent.ACTION_BOOT_COMPLETED) == true || action?.equals("Ishant-RestartService-Broadcast") == true) {
            context?.let { restartService(context = context) }
        }
    }

    private fun restartService(context: Context) {
        context.keepAliveService()
    }
}