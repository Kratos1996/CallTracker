package com.ishant.calltracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.readPhoneNumberPermission
import com.ishant.calltracker.utils.readPhoneStatePermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {
    @Inject
    lateinit var contactUseCase: ContactUseCase
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var phoneStateListener: PhoneStateListener

    override fun onBind(intent: Intent?): IBinder? {
        return null

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
              /*  phoneStateListener = object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        when (state) {
                            TelephonyManager.CALL_STATE_RINGING -> {
                                // Incoming call
                                Log.e("CallTracker", "Call Tracker Service : Call ended")
                            }

                            TelephonyManager.CALL_STATE_OFFHOOK -> {
                               //outgoing
                                Log.e("CallTracker", "Call Tracker Service  : Call ended")
                            }

                            TelephonyManager.CALL_STATE_IDLE -> {
                                // Call ended
                                Log.e("CallTracker", "Call Tracker : Call ended")
                            }
                        }
                    }
                }
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)*/
            })
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener when the service is destroyed
       /// telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }
}