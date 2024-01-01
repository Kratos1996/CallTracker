package com.ishant.calltracker.receiver
//009631
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
/*Ishant Sharma*/
import android.telephony.TelephonyManager
import android.util.Log
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
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
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (AppPreference.isUserLoggedIn) {
                        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        if (phoneNumber != null) {
                            val data = LastCallDetailsCollector()
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(2000)
                                val callerData = data.collectLastCallDetails(context)
                                callerData.collectLatest { dataCaller ->
                                    if (dataCaller != null) {
                                        val dataContact  =  databaseRepository.getRestrictedContact(phone = Utils.extractLast10Digits(dataCaller.callerNumber), isFav = true)
                                        when (dataCaller.callType) {
                                            "Unknown" -> {
                                                Log.e(
                                                    "CallTracker : ",
                                                    "Call Tracker CallEnded ${dataCaller.callerNumber}"
                                                )
                                                if(dataContact == null || dataContact.isFav == false) {
                                                    saveContact(
                                                        phoneNumber = dataCaller.callerNumber,
                                                        sourceMobileNo = getPhoneNumber(),
                                                        name = dataCaller.callerName ?: "Unknown",
                                                        type = "Call Ended without Pickup"
                                                    )
                                                }
                                            }

                                            else -> {
                                                if (dataContact == null ||dataContact.isFav == false) {
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
                    CallTrackerApplication.isRefreshUi.value = true
                    delay(500)
                    CallTrackerApplication.isRefreshUi.value = false
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
                    delay(500)
                    CallTrackerApplication.isRefreshUi.value = true
                    delay(500)
                    CallTrackerApplication.isRefreshUi.value = false
                }
            }
        }.launchIn(
            CoroutineScope(Dispatchers.Main)
        )
    }
}
