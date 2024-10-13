package com.ishant.callsoftware.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ishant.callsoftware.service.CallService
import com.ishant.callsoftware.service.ServiceRestarterService
import com.ishant.callsoftware.utils.isServiceRunning
import com.ishant.callsoftware.utils.navToCallService


class ServiceCheckWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        if (!context.isServiceRunning(CallService::class.java)) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : Workmanger > ServiceCheckWorker > doWork > CallService is not running. Restarting...")
            context.navToCallService()
        }
        else{
            Log.e(ServiceRestarterService.TAG, "CallTracker : Workmanger > ServiceCheckWorker > doWork > CallService service is running....")

        }


        return Result.success()
    }

}