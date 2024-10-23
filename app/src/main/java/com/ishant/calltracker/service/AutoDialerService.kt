package com.ishant.calltracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.constant.Constant
import com.ishant.calltracker.utils.helper.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoDialerService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private var contactsList: List<String> = emptyList()
    private var currentIndex = 0
    private var isCallActive = false
    private var scope = CoroutineScope(Dispatchers.IO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        contactsList = intent?.getStringArrayListExtra("contacts") ?: emptyList()
        currentIndex = 0
        if (contactsList.isNotEmpty()) {
            setupCallListener()
            dialNextContact()
        } else {
            stopSelf() // No contacts to dial, stop the service
        }
        return START_NOT_STICKY
    }

    private fun setupCallListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use TelephonyCallback for API level 31+
            telephonyManager.registerTelephonyCallback(mainExecutor, callStateCallback)
        } else {
            // Use PhoneStateListener for API level < 31
            @Suppress("DEPRECATION")
            telephonyManager.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleCallState(state)
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    // For Android 31 (API level 31+) use TelephonyCallback
    private val callStateCallback =
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                handleCallState(state)
            }
        }

    private fun handleCallState(state: Int) {
        Log.d("TAG", "handleCallState: " + state)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                if (isCallActive) {
                    isCallActive = false
                    currentIndex++
                    if (currentIndex < contactsList.size) {
                        scope.launch {
                            delay(if(AppPreference.autoDialerDelay == 0L) Constant.DEFALT_AUTO_DIALER_DELAY else (AppPreference.autoDialerDelay*1000L))
                            dialNextContact()
                        }

                    } else {
                        stopSelf() // No more contacts to call, stop the service
                    }
                }
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call is active
                isCallActive = true
            }
        }
    }

    private fun dialNextContact() {
        if (currentIndex < contactsList.size) {
            val phoneNumber = contactsList[currentIndex]
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(callIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Unregister TelephonyCallback for API level 31+
            telephonyManager.unregisterTelephonyCallback(callStateCallback)
        } else {
            // Stop listening for older API versions
            @Suppress("DEPRECATION")
            telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE)
        }
    }
}
