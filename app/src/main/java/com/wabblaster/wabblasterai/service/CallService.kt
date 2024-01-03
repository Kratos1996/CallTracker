package com.wabblaster.wabblasterai.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.wabblaster.wabblasterai.domain.ContactUseCase
import com.wabblaster.wabblasterai.utils.readPhoneNumberPermission
import com.wabblaster.wabblasterai.utils.readPhoneStatePermission
import dagger.hilt.android.AndroidEntryPoint
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