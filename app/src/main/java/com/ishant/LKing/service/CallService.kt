package com.ishant.LKing.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import com.ishant.LKing.api.request.UploadContactRequest
import com.ishant.LKing.app.CallTrackerApplication
import com.ishant.LKing.database.room.DatabaseRepository
import com.ishant.LKing.database.room.UploadContact
import com.ishant.LKing.database.room.UploadContactType
import com.ishant.LKing.di.BaseUrlInterceptor
import com.ishant.LKing.domain.ContactUseCase
import com.ishant.LKing.network.Resource
import com.ishant.LKing.receiver.LastCallDetailsCollector
import com.ishant.LKing.receiver.PhoneCallReceiver
import com.ishant.LKing.utils.AppPreference
import com.ishant.LKing.utils.callForegroundService
import com.ishant.LKing.utils.isServiceRunning
import com.ishant.LKing.utils.navToCallService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                    // telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                    telephonyManager =
                        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    callForegroundService() { notificationId, notification ->
                        startForeground(notificationId, notification)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val telephonyManager =
                            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        myTelephonyCallback = MyTelephonyCallback(this)
                        telephonyManager.registerTelephonyCallback(
                            mainExecutor,
                            myTelephonyCallback!!
                        )
                    } else {
                        telephonyManager.listen(
                            phoneStateListener,
                            PhoneStateListener.LISTEN_CALL_STATE
                        )
                        callStateReceiver = PhoneCallReceiver()
                        val filter = IntentFilter().apply {
                            addAction("android.intent.action.PHONE_STATE")
                        }
                        registerReceiver(callStateReceiver, filter)
                    }
                })
            })

        } catch (e: Exception) {

        }

        return START_STICKY
    }

    inner class LocalBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun handleCallData(phoneNumber: String, context: Context) {
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
        if (uploadContacts?.data?.isNotEmpty() == true) {
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
            Log.e(
                ServiceRestarterService.TAG,
                "CallTracker : Service > CallService > TaskRemoved > CallService is not running. Restarting..."
            )
            navToCallService()
        } else {
            Log.e(
                ServiceRestarterService.TAG,
                "CallTracker : Service > CallService > TaskRemoved > CallService service is running...."
            )
        }
        super.onTaskRemoved(rootIntent)
    }

    private var myTelephonyCallback: MyTelephonyCallback? = null
    private var callStateReceiver: PhoneCallReceiver? = null


    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.unregisterTelephonyCallback(myTelephonyCallback!!)
        } else {
            unregisterReceiver(callStateReceiver)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
    inner class MyTelephonyCallback(private val context: Context) : TelephonyCallback(),
        TelephonyCallback.CallStateListener {

        override fun onCallStateChanged(state: Int) {
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    /*      val call: Call = telecomManager.getCurrentCall()
                                if (call != null) {
                                    val phoneNumber: String = call.details.handle.schemeSpecificPart
                                    Log.d("Incoming Call", "Phone Number: $phoneNumber")
                                }*/

                    val phoneNumber = telephonyManager.line1Number
                    //    handleCallData(phoneNumber ?: "", this@CallService)
                }

            }
        }
    }

}