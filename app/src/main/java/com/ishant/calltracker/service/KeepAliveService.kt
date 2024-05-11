package com.ishant.calltracker.service

import android.Manifest
import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ishant.calltracker.receiver.NotificationServiceRestartReceiver
import com.ishant.calltracker.utils.isServiceRunning

class KeepAliveService : Service() {
    override fun onCreate() {
        Log.e("DEBUG", "KeepAliveService onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("DEBUG", "CallTracker : KeepAliveService onStartCommand")

        startNotificationService()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.e("DEBUG", "CallTracker : KeepAliveService onBind")
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.e("DEBUG", "CallTracker : KeepAliveService onUnbind")
        tryReconnectService()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DEBUG", "CallTracker : KeepAliveService onDestroy")
        tryReconnectService()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.e("DEBUG", "CallTracker : KeepAliveService onTaskRemoved")
        tryReconnectService()
    }

    private fun tryReconnectService() {
        Log.e("DEBUG", "CallTracker : KeepAliveService tryReconnectService")
        //Send broadcast to restart service
        val broadcastIntent = Intent(applicationContext, NotificationServiceRestartReceiver::class.java)
        broadcastIntent.action = "Ishant-RestartService-Broadcast"
        sendBroadcast(broadcastIntent)
    }

    private fun startNotificationService() {
        if (!isMyServiceRunning) {
            Log.e("DEBUG", "CallTracker : KeepAliveService startNotificationService")
            val mServiceIntent = Intent(this, CallService::class.java)
            startService(mServiceIntent)
        }
        if (!isServiceRunning(NotificationReaderService::class.java)) {
                val mServiceIntent = Intent(this, NotificationReaderService::class.java)
                startService(mServiceIntent)
        }
    }

    private val isMyServiceRunning: Boolean
        get() {
            val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (CallService::class.java.equals(service.service.className)) {
                    Log.e("isMyServiceRunning?", true.toString() + "")
                    return true
                }
            }
            Log.e("isMyServiceRunning?", false.toString() + "")
            return false
        }
}