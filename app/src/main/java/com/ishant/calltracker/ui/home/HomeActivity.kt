package com.ishant.calltracker.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.room.InvalidationTracker
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.receiver.ContactObserver
import com.ishant.calltracker.receiver.ServiceCheckReceiver
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.TelephonyManagerPlus
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.dataclassesUtils.TelePhoneManager
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.navToRestrictContactActivity
import com.ishant.calltracker.utils.navToUploadContactActivity
import com.ishant.calltracker.utils.serviceContactUploadRestarter
import com.ishant.calltracker.workmanager.ServiceCheckWorker
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import requestNotificationPermission
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var managerPlus: TelephonyManagerPlus
    private lateinit var binding: ActivityHomeBinding
    private lateinit var autoUpdateContactObserver : ContactObserver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        autoUpdateContactObserver =  ContactObserver(this , Handler())
        autoUpdateContactObserver.registerObserver()
        addAutoStartup()
        binding.name.text = AppPreference.user.name?:""
        binding.emailName.text = AppPreference.user.email?:""
        binding.number.text = AppPreference.user.mobile?:""
        binding.phoneStatePermission.setOnClickListener {
            takeCallLogsPermission()
        }
        binding.phoneCallLogsPermission.setOnClickListener {
            takeCallLogsPermission()
        }
        binding.url.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url)))
            startActivity(browserIntent)
        }
    }

    override fun onStop() {
        super.onStop()
        autoUpdateContactObserver.unregisterObserver()
    }

    override fun onResume() {
        super.onResume()
        takeCallLogsPermission()

    }

    private fun takePhoneNetworkPermission() {
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                startAlarmManager()
                //startWorkManager()
                binding.uploadCallonApi.visibility = View.VISIBLE
                binding.addToRestrictedBtn.visibility = View.VISIBLE
                loadUi()

            }) {
                binding.phoneCallLogsPermission.visibility = View.VISIBLE

            }
        }) {
            binding.phoneStatePermission.visibility = View.VISIBLE
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
    }
    private fun loadUi(){
        val data = managerPlus.getSimCardPhoneNumbers(this)
        binding.phoneStatePermission.visibility = View.GONE
        if(!data.isNullOrEmpty()) {
            if (data.isEmpty()) {
                binding.simEmptyView.visibility = View.VISIBLE
                binding.dualSimUi.visibility = View.GONE
                binding.singleSimUi.visibility = View.GONE
            }
        }
        else{
            binding.phoneStatePermission.visibility = View.VISIBLE
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
        AppPreference.simManager = TelePhoneManager(data)
        binding.addToRestrictedBtn.setOnClickListener {
            navToRestrictContactActivity()
        }
        binding.uploadCallonApi.setOnClickListener {
            navToUploadContactActivity()
        }
    }

    private fun takeCallLogsPermission(){
        readPhoneContactPermission(
            granted = {
                //serviceContact()
                readPhoneLogPermission(granted = {
                    binding.phoneCallLogsPermission.visibility = View.GONE
                    takePhoneNetworkPermission()
                }){
                    if(!isNotificationPermissionGranted(this)) {
                        requestNotificationPermission(this)
                    }else{
                        if (!isServiceRunning(CallService::class.java)) { // Replace with your service class
                             navToCallService()
                        }
                    }
                    binding.phoneCallLogsPermission.visibility = View.VISIBLE
                }
            }
        ){
            binding.addToRestrictedBtn.visibility = View.GONE
            Toast.makeText(this,"Need Contact Permission",Toast.LENGTH_SHORT).show()
        }

    }

   /* private fun startWorkManager(){

        val serviceCheckWorkRequest = PeriodicWorkRequest.Builder(
            ServiceCheckWorker::class.java,
            15, // Repeat interval in minutes
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueue(serviceCheckWorkRequest)

// Optional: Observe the result of the worker
        WorkManager.getInstance(applicationContext)
            .getWorkInfoByIdLiveData(serviceCheckWorkRequest.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.e(ServiceRestarterService.TAG, "CallTracker : HomeActivity > ServiceCheckWorker > doWork > CallService service is running....")
                }
            })
    }*/

    private fun startAlarmManager(){
        // Schedule the alarm to run every minute
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ServiceCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val intervalMillis = 60 * 1000L  // 1 minute
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }
}