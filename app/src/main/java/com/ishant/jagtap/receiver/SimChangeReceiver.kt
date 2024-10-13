package com.ishant.jagtap.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import com.ishant.jagtap.service.CallService
import com.ishant.jagtap.service.ServiceRestarterService
import com.ishant.jagtap.utils.AppPreference
import com.ishant.jagtap.utils.TelephonyManagerPlus
import com.ishant.jagtap.utils.dataclassesUtils.TelePhoneManager
import com.ishant.jagtap.utils.isServiceRunning
import com.ishant.jagtap.utils.navToCallService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SimChangeReceiver : BroadcastReceiver() {
    @Inject
    lateinit var managerPlus: TelephonyManagerPlus
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_SIM_STATE_CHANGED == action) {
            val extras = intent.extras
            printExtras(extras)
            val state = extras!!.getString(EXTRA_SIM_STATE)
            Log.w(
                TAG,
                "SIM Action : $action / State : $state"
            )
            // Test phoneName = GSM ?
            if (ACTION_SIM_STATE_CHANGED == state) {
                val data = managerPlus.getSimCardPhoneNumbers(context)
                AppPreference.simManager = TelePhoneManager(data)
                if (!context.isServiceRunning(CallService::class.java)) { // Replace with your service class
                    Log.e(ServiceRestarterService.TAG, "PhoneCallReceiver : Receiver > PhoneCallReceiver > startServiceMonitoring > CallService is not running. Restarting...")
                    context.navToCallService()
                }else{
                    Log.e(ServiceRestarterService.TAG, "PhoneCallReceiver : Receiver > PhoneCallReceiver > startServiceMonitoring > CallService service is running....")
                }
            }
        }
    }
    private fun printExtras(extras: Bundle?) {
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras[key]
                Log.d(
                    TAG,
                    "EventSpy SIM extras : $key = $value"
                )
            }
        }
    }

    companion object {
        private const val TAG = "SimChangeReceiver"
        private const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
        private const val EXTRA_SIM_STATE = "ss"
        private const val SIM_STATE_LOADED = "LOADED"
        private fun getSystemPhoneNumber(context: Context): String {
            // Read Phone number
            val telephoneMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
          /*  val phoneNumber = telephoneMgr.line1Number*/
           /* Log.d(TAG, "EventSpy SIM DeviceId : " + telephoneMgr.deviceId) // Code IMEI
            Log.d(TAG, "EventSpy SIM Network Operator Name : " + telephoneMgr.networkOperatorName)
            Log.d(TAG, "EventSpy SIM Serial Number : " + telephoneMgr.simSerialNumber)
            Log.d(TAG, "EventSpy SIM PhoneNumber : $phoneNumber") // Code IMEI
            */return ""
        }

    }
}