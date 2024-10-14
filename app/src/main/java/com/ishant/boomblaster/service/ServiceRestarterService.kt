package com.ishant.boomblaster.service
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ishant.boomblaster.utils.callForegroundService
import com.ishant.boomblaster.utils.navToCallService

class ServiceRestarterService : Service() {

    companion object {
        const val TAG = "CallTracker :"
        const val SERVICE_TO_RESTART = "com.ishant.boomblaster.service.CallService" // Replace with your service name
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "ServiceRestarterService started")
        Log.e(TAG, "CallTracker : Service > ServiceRestarterService > onStartCommand > Service Started")
        // Start monitoring the target service
        startServiceMonitoring()

        return START_STICKY
    }

    private fun startServiceMonitoring() {
        if (!isServiceRunning(CallService::class.java)) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > CallService is not running. Restarting...")
            navToCallService()
        }
        else{
            callForegroundService(){ notificationId,notification ->
                startForeground(notificationId, notification)
            }
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > CallService service is running....")
        }
        scheduleServiceCheck()
    }

    private fun scheduleServiceCheck() {
        val intervalMillis = 29000L // 1 minute interval, adjust as needed
        val handler = android.os.Handler()

        handler.postDelayed(object : Runnable {
            override fun run() {
                startServiceMonitoring()
                handler.postDelayed(this, intervalMillis)
            }
        }, intervalMillis)
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val services = activityManager?.getRunningServices(Int.MAX_VALUE)

        if (services != null) {
            for (service in services) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "CallTracker : Service > ServiceRestarterService > Destroy")
    }
}