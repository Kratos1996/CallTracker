package com.ishant.calltracker.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import checkPermission
import com.ishant.calltracker.R
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.navigation.navhost.host.dashboard.HomeNavHost
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.service.NotificationReaderService
import com.ishant.calltracker.service.WhatsappAccessibilityService
import com.ishant.calltracker.utils.NotificationListenerUtil
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.isAccessibilityOn
import com.ishant.calltracker.utils.isBatteryOptimizationIgnored
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.openAccessibilitySettings
import com.ishant.calltracker.utils.requestBatteryOptimizationPermission
import com.ishant.calltracker.utils.startAlarmManager
import com.ishant.calltracker.utils.startWorkManager
import com.ishant.calltracker.utils.wpService
import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.theme.CoreTheme
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import requestNotificationPermission

@AndroidEntryPoint
class DashboardActivity : BaseComposeActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            readPhoneContactPermission(granted = {
                viewModel.loadContactObserver(this)
            })



        val data = viewModel.managerPlus.getSimCardPhoneNumbers(this)
        takePhoneNetworkPermission()

        notificationListenerUtil = NotificationListenerUtil(this)
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                if (granted) {
                    val mServiceIntent = Intent(this, NotificationReaderService::class.java)

                    startService(mServiceIntent)

                }
            }

        if (!this.isAccessibilityOn(WhatsappAccessibilityService::class.java)) {
            this.openAccessibilitySettings()
        }
        addAutoStartup()
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                viewModel.loadSimInfo(this)
                if (!isServiceRunning(KeepAliveService::class.java)) { // Replace with your service class
                    keepAliveService()
                    wpService()
                    startWorkManager(this)
                    startAlarmManager()
                    viewModel.managers.value = true
                    viewModel.callService.value = true
                } else {
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
        viewModel.loadSimInfo(this)
        viewModel.getWhatsappList()
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
    }

     fun readNotificationService() {
        if (!notificationListenerUtil.isNotificationServiceEnabled()) {
            notificationListenerUtil.requestNotificationListenerPermission(
                notificationListenerPermissionLauncher
            )
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

    private fun takeCallLogsPermission() {
        readPhoneContactPermission(
            granted = {
                viewModel.contactPermissionGranted.value = true
                readPhoneLogPermission(granted = {
                    viewModel.phoneLogsPermissionGranted.value = true
                }) {
                    if (!isNotificationPermissionGranted(this)) {
                        requestNotificationPermission(this)
                    } else {
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