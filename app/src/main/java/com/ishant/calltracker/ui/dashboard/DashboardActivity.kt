package com.ishant.calltracker.ui.dashboard

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ishant.calltracker.R
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.receiver.ServiceCheckReceiver
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.ui.navhost.host.dashboard.HomeNavHost
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.workmanager.ServiceCheckWorker
import com.ishant.corelibcompose.toolkit.ui.theme.CoreTheme
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
class DashboardActivity : BaseComposeActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadContactObserver(this)
        val data = viewModel.managerPlus.getSimCardPhoneNumbers(this)
        takePhoneNetworkPermission()
        setContent {
            CoreTheme(
                darkTheme = CallTrackerApplication.isDark.value
            ) {
                HomeNavHost(
                    homeViewModel = viewModel,
                    onNavigate = { nav ->

                    }
                )
            }
        }
        addAutoStartup()
        startWorkManager()
        startAlarmManager()
    }

    private fun takePhoneNetworkPermission() {
        readPhoneStatePermission(granted = {
            viewModel.readPhoneStatePermissionGranted.value = true
            readPhoneNumberPermission(granted = {
                viewModel.phoneNumberPermissionGranted.value = true
                takeCallLogsPermission()
            })
        })
    }

    private fun takeCallLogsPermission(){
        readPhoneContactPermission(
            granted = {
                viewModel.contactPermissionGranted.value = true
                readPhoneLogPermission(granted = {
                    viewModel.phoneLogsPermissionGranted.value = true
                }){
                    if(!isNotificationPermissionGranted(this)) {
                        requestNotificationPermission(this)
                    }else{
                        if (!isServiceRunning(CallService::class.java)) { // Replace with your service class
                            startAlarmManager()
                            navToCallService()
                        }
                    }

                }
            }
        )
    }

    private fun startWorkManager(){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ServiceCheckWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
// Optional: Observe the result of the worker
        WorkManager.getInstance(applicationContext)
            .getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.e(ServiceRestarterService.TAG, "CallTracker : HomeActivity > ServiceCheckWorker > doWork > CallService service is running....")
                }
            })
    }

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