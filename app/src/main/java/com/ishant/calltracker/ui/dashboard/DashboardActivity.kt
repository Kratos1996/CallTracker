package com.ishant.calltracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import com.ishant.calltracker.R
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.service.NotificationReaderService
import com.ishant.calltracker.service.WhatsappAccessibilityService
import com.ishant.calltracker.navigation.navhost.host.dashboard.HomeNavHost
import com.ishant.calltracker.utils.NotificationListenerUtil
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.isAccessibilityOn

import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.openAccessibilitySettings

import com.ishant.calltracker.utils.startAlarmManager
import com.ishant.calltracker.utils.startWorkManager

import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.theme.CoreTheme
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import readPostNotificationPermission
import requestNotificationPermission

@AndroidEntryPoint
class DashboardActivity : BaseComposeActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>
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

        viewModel.loadSimInfo(this)
        viewModel.getWhatsappList()
        notificationListenerUtil = NotificationListenerUtil(this)
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                if(granted){
                    val mServiceIntent = Intent(this, NotificationReaderService::class.java)

                    startService(mServiceIntent)

                }
            }
        readNotificationService()
        readPostNotificationPermission(granted = {
            viewModel.notificationPermissionGranted.value = true
        }, rejected = {
            viewModel.notificationPermissionGranted.value = false
        })

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

    private fun readNotificationService() {
        if(!notificationListenerUtil.isNotificationServiceEnabled()){
            notificationListenerUtil.requestNotificationListenerPermission(notificationListenerPermissionLauncher)
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