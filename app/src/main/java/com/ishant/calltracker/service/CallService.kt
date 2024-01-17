package com.ishant.calltracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ishant.calltracker.R
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.receiver.LastCallDetailsCollector
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Utils
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

@AndroidEntryPoint
class CallService : Service() {
    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var databaseRepository: DatabaseRepository


    private lateinit var telephonyManager: TelephonyManager
    private val notificationId = 1
    private val channelId = "call_listener_channel"

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when(state){
                TelephonyManager.CALL_STATE_IDLE-> {
                    handleCallData(phoneNumber?:"",this@CallService)
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                startForegroundService()
                registerPhoneStateListener()
            })
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterPhoneStateListener()
    }

    private fun startForegroundService() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("${getString(R.string.app_name)} is Working... ")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Listener Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun registerPhoneStateListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun unregisterPhoneStateListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }
    private fun handleCallData(phoneNumber: String, context: Context) {
        if (phoneNumber.isNotEmpty()) {
            if (AppPreference.isUserLoggedIn) {
                val data = LastCallDetailsCollector()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    val callerData = data.collectLastCallDetails(context)
                    callerData.collectLatest { dataCaller ->
                        if (dataCaller != null) {
                            val dataContact = databaseRepository.getRestrictedContact(
                                phone = Utils.extractLast10Digits(dataCaller.callerNumber),
                                isFav = true
                            )
                            when (dataCaller.callType) {
                                "Unknown" -> {
                                    Log.e(
                                        "CallTracker : ",
                                        "Call Tracker CallEnded ${dataCaller.callerNumber}"
                                    )
                                    if (dataContact == null || dataContact.isFav == false) {
                                        saveContact(
                                            phoneNumber = dataCaller.callerNumber,
                                            sourceMobileNo = getPhoneNumber(),
                                            name = dataCaller.callerName ?: "Unknown",
                                            type = "Call Ended without Pickup"
                                        )
                                    }
                                }

                                else -> {
                                    if (dataContact == null || dataContact.isFav == false) {
                                        saveContact(
                                            phoneNumber = dataCaller.callerNumber,
                                            sourceMobileNo = getPhoneNumber(),
                                            name = dataCaller.callerName ?: "Unknown",
                                            type = dataCaller.callType
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getPhoneNumber(): String {
        when (AppPreference.simManager.data.size) {
            1 -> {
                return if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) {
                    AppPreference.user.mobile ?: ""
                } else {
                    AppPreference.simManager.data[0].phoneNumber
                }
            }

            2 -> {
                return if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty() && AppPreference.simManager.data[1].phoneNumber.isNullOrEmpty()) {
                    AppPreference.user.mobile ?: ""
                } else if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) {
                    AppPreference.user.mobile ?: ""
                } else {
                    AppPreference.simManager.data[0].phoneNumber
                }
            }

            else -> {
                return AppPreference.user.mobile ?: ""
            }
        }
    }

    private fun saveContact(
        phoneNumber: String,
        name: String,
        sourceMobileNo: String,
        type: String
    ) {
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(sourceMobileNo),
            mobile = Utils.extractLast10Digits(phoneNumber),
            name = /*AppPreference.user.name ?: ""*/name,
            type = type
        ).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    Log.e("CallTracker : ", "CallTracker: Contact Not Saved")
                    val data = UploadContact(serialNo = System.currentTimeMillis(),
                        sourceMobileNo = sourceMobileNo,
                        mobile = phoneNumber,
                        name = name,
                        type = UploadContactType.PENDING,
                    )
                    databaseRepository.insertUpload(data)
                    delay(1000)
                    CallTrackerApplication.isRefreshUi.value = true
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    Log.e("CallTracker : ", "CallTracker: Contact Saved")
                    val data = UploadContact(
                        serialNo = System.currentTimeMillis(),
                        sourceMobileNo = sourceMobileNo,
                        mobile = phoneNumber,
                        name = name,
                        type = UploadContactType.COMPLETE
                    )
                    databaseRepository.insertUpload(data)
                    delay(1000)
                    CallTrackerApplication.isRefreshUi.value = true
                }
            }
        }.launchIn(
            CoroutineScope(Dispatchers.Main)
        )
    }
}