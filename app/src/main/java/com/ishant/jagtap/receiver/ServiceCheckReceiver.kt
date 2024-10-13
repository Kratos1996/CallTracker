package com.ishant.jagtap.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ishant.jagtap.service.CallService
import com.ishant.jagtap.service.KeepAliveService
import com.ishant.jagtap.service.ServiceRestarterService
import com.ishant.jagtap.utils.isServiceRunning
import com.ishant.jagtap.utils.navToCallService

class ServiceCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context?.isServiceRunning(CallService::class.java) == false) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : ServiceCheckReceiver > CallService > onReceive > CallService is not running. Restarting...")
            context.navToCallService()
        }
        else{
            Log.e(ServiceRestarterService.TAG, "CallTracker : ServiceCheckReceiver > CallService > onReceive > CallService service is running....")
        }

        if (context?.isServiceRunning(KeepAliveService::class.java) == false) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : ServiceCheckReceiver > ServiceAlive > onReceive > ServiceAlive is not running. Restarting...")
            context.navToCallService()
        }
        else{
            Log.e(ServiceRestarterService.TAG, "CallTracker : ServiceCheckReceiver > ServiceAlive > onReceive > ServiceAlive service is running....")
        }
    }
}