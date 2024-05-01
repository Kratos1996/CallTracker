package com.ishant.calltracker.ui.dashboard

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ishant.calltracker.R
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.receiver.ServiceCheckReceiver
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.ui.navhost.host.dashboard.HomeNavHost
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.callForegroundService
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.startAlarmManager
import com.ishant.calltracker.utils.startWorkManager
import com.ishant.calltracker.workmanager.ServiceCheckWorker
import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.theme.CoreTheme
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import requestNotificationPermission
import takeForegroundContactService
import java.util.concurrent.TimeUnit

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
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                if (!isServiceRunning(KeepAliveService::class.java)) { // Replace with your service class
                    keepAliveService()
                    startWorkManager(this)
                    startAlarmManager()
                    viewModel.managers.value = true
                    viewModel.callService.value = true
                }else{
                    viewModel.managers.value = true
                    viewModel.callService.value = true
                }
            })
        })
        onBackPressedWaAppBlaster(this) {
            showAsBottomSheet { dismiss ->
                CommonAlertBottomSheet(msg = stringResource(R.string.do_you_want_to_close_application),
                    positiveText = stringResource(R.string.yes),
                    onPositiveClick = {
                        viewModel.navigateBack()
                    },
                    negativeText = stringResource(R.string.no),
                    onNegativeClick = {
                        dismiss.invoke()
                    }
                )
            }
        }
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



}