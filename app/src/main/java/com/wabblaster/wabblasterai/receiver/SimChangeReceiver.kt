package com.wabblaster.wabblasterai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import com.wabblaster.wabblasterai.utils.AppPreference
import com.wabblaster.wabblasterai.utils.TelephonyManagerPlus
import com.wabblaster.wabblasterai.utils.dataclassesUtils.TelePhoneManager
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