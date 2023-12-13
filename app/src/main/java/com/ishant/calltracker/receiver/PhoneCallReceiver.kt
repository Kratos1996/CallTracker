package com.ishant.calltracker.receiver
//009631
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
/*Ishant Sharma*/
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.ui.home.CallService
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Utils
import com.ishant.calltracker.utils.navToCallService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contactUseCase: ContactUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.e("CallTracker : ", "Call Tracker ongoing  ")
                   /* val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    var simSlotIndex = intent.getIntExtra("simSlotIndex", -1)
                    Log.e("CallTracker : ", "Call Tracker outgoing  $phoneNumber")
                    if (phoneNumber != null) {
                        saveContact( phoneNumber,getPhoneNumber(),"Outgoing Call" )
                    }*/
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (phoneNumber != null) {
                        val data = LastCallDetailsCollector()
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            val callerData = data.collectLastCallDetails(context)
                            callerData.collectLatest { dataCaller ->
                                if(dataCaller!=null){
                                    Log.e("CallTracker : ", "Call Tracker CallType :  ${dataCaller.callType}")
                                    when (dataCaller.callType) {
                                        "Outgoing" -> {
                                            Log.e("CallTracker : ", "Call Tracker Outgoing ${dataCaller.callerNumber}")
                                            saveContact( phoneNumber = dataCaller.callerNumber,
                                                sourceMobileNo =  getPhoneNumber(),
                                                name = AppPreference.user.name?:"",
                                                type = dataCaller.callType )
                                        }
                                        "Unknown" -> {
                                            Log.e("CallTracker : ", "Call Tracker CallEnded ${dataCaller.callerNumber}")
                                            saveContact( phoneNumber = getPhoneNumber(),
                                                sourceMobileNo = dataCaller.callerNumber ,
                                                name = dataCaller.callerName?:"Unknown",
                                                type = "Call Ended without Pickup" )
                                        }
                                        else -> {
                                            Log.e("CallTracker : ", "Call Tracker Incoming ${dataCaller.callerNumber}")
                                            saveContact( phoneNumber = getPhoneNumber(),
                                                sourceMobileNo = dataCaller.callerNumber ,
                                                name = dataCaller.callerName?:"Unknown",
                                                type = dataCaller.callType )
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
    private fun getPhoneNumber():String
    {
        when (AppPreference.simManager.data.size) {
            1 -> {
                return if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()){
                    AppPreference.user.mobile?:""
                }else{
                    AppPreference.simManager.data[0].phoneNumber
                }
            }
            2 -> {
                return if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty() &&  AppPreference.simManager.data[1].phoneNumber.isNullOrEmpty()){
                    AppPreference.user.mobile?:""
                }else if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()){
                    AppPreference.user.mobile?:""
                }else{
                    AppPreference.simManager.data[0].phoneNumber
                }
            }
            else -> {
                return AppPreference.user.mobile?:""
            }
        }
    }

    private fun saveContact(phoneNumber: String,name :String,sourceMobileNo:String,type:String) {
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(sourceMobileNo),
            mobile = Utils.extractLast10Digits(phoneNumber),
            name = /*AppPreference.user.name ?: ""*/name,
            type = type
        ).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    Log.e("CallTracker : ", "CallTracker: Contact Not Saved")
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    Log.e("CallTracker : ", "CallTracker: Contact Saved")
                }
            }
        }.launchIn(
            CoroutineScope(Dispatchers.Default)
        )
    }
}
