package com.ishant.calltracker.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import checkPermission
import com.ishant.calltracker.service.AutoDialerService
import readCallPermission

object AutoDialerHelper {
     fun checkCallPermissionAndStartDialer(context: Context, contacts: ArrayList<String>) {

        context.readCallPermission(granted = {
            startAutoDialerService(context,contacts)
        }, rejected = {

        })


    }


    private fun startAutoDialerService(context: Context,contacts: ArrayList<String>) {
        val intent = Intent(context, AutoDialerService::class.java)
        intent.putStringArrayListExtra("contacts", contacts)
        context.startService(intent)
    }
}