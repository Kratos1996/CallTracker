package com.ishant.calltracker.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.receiver.LastCallDetailsCollector
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Utils
import com.ishant.calltracker.utils.callForegroundService
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.serviceContactUploadRestarter
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

    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    private var handler: Handler? = null
    private var timeoutRunnable: Runnable? = null
    var counter: MutableLiveData<Int> = MutableLiveData(0)

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
                    serviceContactUploadRestarter()
                })
            })
        } catch (e: Exception) {
            /*callForegroundService(){notificationId,notification ->
                startForeground(notificationId, notification)
            }*/
            serviceContactUploadRestarter()
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
                                            type = "Call Ended without Pickup",
                                            duration = dataCaller.duration
                                        )
                                    }
                                }

                                else -> {
                                    if (dataContact == null || dataContact.isFav == false) {
                                        saveContact(
                                            phoneNumber = dataCaller.callerNumber,
                                            sourceMobileNo = getPhoneNumber(),
                                            name = dataCaller.callerName ?: "Unknown",
                                            type = dataCaller.callType,
                                            duration = dataCaller.duration
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
        type: String,
        duration:String
    ) {
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(sourceMobileNo),
            mobile = Utils.extractLast10Digits(phoneNumber),
            name = /*AppPreference.user.name ?: ""*/name,
            type = type,
            duration = duration
        ).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    Log.e("CallTracker : ", "CallTracker: Contact Not Saved")
                    val data = UploadContact(
                        serialNo = System.currentTimeMillis(),
                        sourceMobileNo = sourceMobileNo,
                        mobile = phoneNumber,
                        name = name,
                        type = UploadContactType.PENDING,
                        duration = duration
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
                        type = UploadContactType.COMPLETE,
                        duration = duration
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

    private fun startCounter() {
        val looper: Looper? = Looper.myLooper()
        looper.let {
            handler = Handler(looper!!)
            timeoutRunnable = Runnable {
                counter.value = counter.value!! + 1
                timeoutRunnable?.let { it1 -> handler?.postDelayed(it1, 3000) }
            }
            timeoutRunnable?.let { handler?.post(it) }

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