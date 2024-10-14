package com.ishant.boomblaster.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ishant.boomblaster.service.CallService
import com.ishant.boomblaster.service.ServiceRestarterService
import com.ishant.boomblaster.utils.isServiceRunning
import com.ishant.boomblaster.utils.navToCallService


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