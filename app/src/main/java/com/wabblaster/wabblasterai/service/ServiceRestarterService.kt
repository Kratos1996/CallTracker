package com.wabblaster.wabblasterai.service
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.wabblaster.wabblasterai.utils.serviceContact

class ServiceRestarterService : Service() {

    companion object {
        const val TAG = "CallTracker :"
        const val SERVICE_TO_RESTART = "com.ishant.calltracker.service.ContactUpdateOnServer" // Replace with your service name
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
        val intent = Intent(this, ContactUpdateOnServer::class.java) // Replace with your service class
        intent.action = SERVICE_TO_RESTART

        if (!isServiceRunning(ContactUpdateOnServer::class.java)) { // Replace with your service class
            Log.e(TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > Target service is not running. Restarting...")
            startService(intent)
        }else{
            Log.e(TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > Target service is running....")
        }
        if (!isServiceRunning(ContactSyncService::class.java)) { // Replace with your service class
            Log.e(TAG, "CallTracker : Service > ServiceRestarterService > ContactServiceRestart ")
          //  serviceContact()
        }
        // Schedule periodic checking
        scheduleServiceCheck()
    }

    private fun scheduleServiceCheck() {
        val intervalMillis = 60000L // 1 minute interval, adjust as needed
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