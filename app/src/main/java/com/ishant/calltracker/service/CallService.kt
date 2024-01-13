package com.ishant.calltracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.ishant.calltracker.domain.ContactUseCase
import dagger.hilt.android.AndroidEntryPoint
import readPhoneNumberPermission
import readPhoneStatePermission
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
            })
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener when the service is destroyed
       /// telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }
}