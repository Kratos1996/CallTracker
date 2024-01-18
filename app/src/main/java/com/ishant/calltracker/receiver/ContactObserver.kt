package com.ishant.calltracker.receiver

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.ContactUpdateOnServer
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import readPhoneContactPermission


class ContactObserver(val context: Context, handler: Handler) : ContentObserver(handler) {

    private val contentResolver = context.contentResolver

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        if (!context.isServiceRunning(CallService::class.java)) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > Target service is not running. Restarting...")
            context.navToCallService()
        }else{
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > ServiceRestarterService > startServiceMonitoring > Target service is running....")
        }
        context.readPhoneContactPermission(
            granted = {
                context.startService(Intent(context, ContactSyncService::class.java))
            })
    }

    fun registerObserver() {
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            this
        )
    }

    fun unregisterObserver() {
        contentResolver.unregisterContentObserver(this)
    }
}