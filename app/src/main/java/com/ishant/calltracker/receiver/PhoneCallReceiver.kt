package com.ishant.calltracker.receiver
//009631
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contactUseCase: ContactUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                // Incoming call detected
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                var simSlotIndex = intent.getIntExtra("simSlotIndex", -1)
                Log.e("CallTracker : ", "Call Tracker IncomingCall : $phoneNumber")
                Log.e("CallTracker : ", "Call Tracker SimSlot: $simSlotIndex")
                // Check SIM details and show notification for SIM 1
                if (phoneNumber != null) {
                    if (AppPreference.isSim1Selected) {
                        Log.e("CallTracker : ", "Call Tracker IncomingCall 4 : $phoneNumber")
                        saveContact(phoneNumber, if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) AppPreference.simManager.data[1].phoneNumber else AppPreference.simManager.data[0].phoneNumber)
                    } else if (AppPreference.isSim2Selected) {
                        saveContact(phoneNumber, if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) AppPreference.simManager.data[1].phoneNumber else AppPreference.simManager.data[0].phoneNumber)
                    }
                }
            } else if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                Log.e("CallTracker : ", "Call Tracker ongoing  ")
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                var simSlotIndex = intent.getIntExtra("simSlotIndex", -1)
                Log.e("CallTracker : ", "Call Tracker outgoing  $phoneNumber")
                if (phoneNumber != null) {
                    if (AppPreference.isSim1Selected) {
                        saveContact(if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) AppPreference.simManager.data[1].phoneNumber else AppPreference.simManager.data[0].phoneNumber , phoneNumber)
                    } else if (AppPreference.isSim2Selected) {
                        saveContact(if(AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) AppPreference.simManager.data[1].phoneNumber else AppPreference.simManager.data[0].phoneNumber, phoneNumber)
                    }
                }
            }
        }
    }

    private fun saveContact(phoneNumber: String,sourceMobileNo:String) {
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(sourceMobileNo),
            mobile = Utils.extractLast10Digits(phoneNumber),
            name = AppPreference.user.name ?: ""
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


    private fun isSim1(context: Context?, simSlotIndex: Int): Boolean {
        return simSlotIndex == 0
    }

    private fun showNotification(context: Context?, title: String, message: String?) {
        // Implement logic to show a notification
        // You can use NotificationCompat or any other notification library
        // ...
    }
}