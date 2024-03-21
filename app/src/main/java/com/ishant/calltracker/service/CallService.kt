package com.ishant.calltracker.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.di.BaseUrlInterceptor
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.receiver.LastCallDetailsCollector
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Utils
import com.ishant.calltracker.utils.callForegroundService
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import readPhoneNumberPermission
import readPhoneStatePermission
import javax.inject.Inject


@Suppress("DEPRECATION")
@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var databaseRepository: DatabaseRepository
    @Inject
    lateinit var baseUrlInterceptor: BaseUrlInterceptor

    private lateinit var telephonyManager: TelephonyManager


    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    handleCallData(phoneNumber ?: "", this@CallService)
                }
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            readPhoneStatePermission(granted = {
                readPhoneNumberPermission(granted = {
                    telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    callForegroundService(){notificationId,notification ->
                        startForeground(notificationId, notification)
                    }
                    registerPhoneStateListener()
                })
            })
        } catch (e: Exception) {
            registerPhoneStateListener()
        }

        return START_STICKY
    }

    inner class LocalBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    override fun onCreate() {
        super.onCreate()
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    private fun registerPhoneStateListener() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val extras = telephonyManager.currentCallExtras
            telephonyManager.registerTelephonyCallback(
                this.mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        when (state) {
                            TelephonyManager.CALL_STATE_IDLE -> {
                                val incomingNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                                if (incomingNumber != null) {
                                    // Log or handle the incoming call number
                                    println("Incoming call number: $incomingNumber")
                                }
                                handleCallData( "", this@CallService)
                            }
                        }
                    }
                })
        } else {

        }*/
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun unregisterPhoneStateListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    private fun handleCallData(phoneNumber: String, context: Context){
        if (AppPreference.isUserLoggedIn && phoneNumber.isNotEmpty()) {
            val data = LastCallDetailsCollector(databaseRepository = databaseRepository)
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                val callerData = data.collectLastCallDetails(context)
                if (callerData != null && callerData.data.isNotEmpty()) {
                    saveContact(callerData)
                }
            }
        }
    }

    private fun saveContact(uploadContacts: UploadContactRequest?) {
        if(uploadContacts?.data?.isNotEmpty() == true){
            baseUrlInterceptor.setBaseUrl(AppPreference.baseUrl)
            contactUseCase.uploadContacts(request = uploadContacts).onEach { result ->
                when (result) {
                    is Resource.Error -> {
                        Log.e("CallTracker : ", "CallTracker: Contact Not Saved")
                        val data = UploadContact(
                            serialNo = System.currentTimeMillis(),
                            listOfCalls = Gson().toJson(uploadContacts),
                            type = UploadContactType.PENDING,
                            date = System.currentTimeMillis()
                        )
                        databaseRepository.insertUpload(data)
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }

                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }
                }
            }.launchIn(
                CoroutineScope(Dispatchers.Main)
            )
        }

    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!isServiceRunning(CallService::class.java)) { // Replace with your service class
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > CallService > TaskRemoved > CallService is not running. Restarting...")
            navToCallService()
        }
        else{
            Log.e(ServiceRestarterService.TAG, "CallTracker : Service > CallService > TaskRemoved > CallService service is running....")
        }
        super.onTaskRemoved(rootIntent)
    }

}