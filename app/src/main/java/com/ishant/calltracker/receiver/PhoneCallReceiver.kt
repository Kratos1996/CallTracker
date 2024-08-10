package com.ishant.calltracker.receiver
//009631
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
/*Ishant Sharma*/
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.di.BaseUrlInterceptor
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.database.AppPreference
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.serviceContactUploadRestarter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    @Inject
    lateinit var baseUrlInterceptor: BaseUrlInterceptor

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    //
                    if (!context.isServiceRunning(CallService::class.java)) { // Replace with your service class
                        handleCallData(intent, context)
                        Log.e(
                            ServiceRestarterService.TAG,
                            "PhoneCallReceiver : Receiver > PhoneCallReceiver > startServiceMonitoring > CallService is not running. Restarting..."
                        )
                        context.serviceContactUploadRestarter()
                    } else {
                        Log.e(
                            ServiceRestarterService.TAG,
                            "PhoneCallReceiver : Receiver > PhoneCallReceiver > startServiceMonitoring > CallService service is running...."
                        )
                      //  handleCallData(intent, context)
                    }
                }

            }
        }

    }

    private fun handleCallData(intent: Intent, context: Context) {
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if ((phoneNumber?:"").isNotEmpty()) {
            if (AppPreference.isUserLoggedIn) {
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
}


