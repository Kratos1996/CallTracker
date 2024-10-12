package com.ishant.calltracker.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.content.UnusedAppRestrictionsConstants.API_30
import androidx.core.content.UnusedAppRestrictionsConstants.API_30_BACKPORT
import androidx.core.content.UnusedAppRestrictionsConstants.API_31
import androidx.core.content.UnusedAppRestrictionsConstants.DISABLED
import androidx.core.content.UnusedAppRestrictionsConstants.ERROR
import androidx.core.content.UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE
import checkPermission
import com.google.common.util.concurrent.ListenableFuture
import com.ishant.calltracker.R
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.navigation.navhost.host.dashboard.HomeNavHost
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.service.NotificationReaderService
import com.ishant.calltracker.utils.NotificationListenerUtil
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.startAlarmManager
import com.ishant.calltracker.utils.startWorkManager
import com.ishant.calltracker.utils.toast
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
import sendSmsPermission
import takeForegroundContactService
import writePhoneContactPermission

@AndroidEntryPoint
class DashboardActivity : BaseComposeActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    lateinit var notificationListenerUtil: NotificationListenerUtil

    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writePhoneContactPermission(granted = {
            takeForegroundContactService(granted = {
                readPhoneContactPermission(granted = {
                    viewModel.loadContactObserver(this)
                    startService(Intent(this, ContactSyncService::class.java))

                }){
                    toast("Need Read Contact Service")
                }
            })
        }){
            Toast.makeText(this,getString(R.string.read_write_contact_permission), Toast.LENGTH_SHORT).show()
        }

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
                UnusedAppRestrictionsCheck()
            }

        }



        viewModel.loadSimInfo(this)
        viewModel.getWhatsappList()
        notificationListenerUtil = NotificationListenerUtil(this)
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                if (granted) {
                    val mServiceIntent = Intent(this, NotificationReaderService::class.java)

                    startService(mServiceIntent)

                }
            }
//        readNotificationService()
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
                } else {
                    viewModel.managers.value = true
                    viewModel.callService.value = true
                }
            })



        })
        sendSmsPermission(granted = {
            viewModel.sendSmsPermissionGranted.value = true
        },
            rejected = {
                viewModel.sendSmsPermissionGranted.value = false
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

    override fun onResume() {
        super.onResume()
        viewModel.contactPermissionGranted.value =
            !this.checkPermission(Manifest.permission.READ_CONTACTS)
        viewModel.phoneLogsPermissionGranted.value =
            !this.checkPermission(Manifest.permission.READ_CALL_LOG)
        viewModel.readPhoneStatePermissionGranted.value =
            !this.checkPermission(Manifest.permission.READ_PHONE_STATE)
        viewModel.phoneNumberPermissionGranted.value =
            !this.checkPermission(Manifest.permission.READ_PHONE_NUMBERS)
        viewModel.notificationPermissionGranted.value =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                !this.checkPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                true
            }
        viewModel.getWhatsappList()
        viewModel.loadSimInfo(this)
    }

    private fun readNotificationService() {
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


// This function directs users to the app settings page
private fun requestUnusedAppPermission(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", "com.ishant.calltracker", null)
    }
    context.startActivity(intent)
}
@Composable
fun UnusedAppRestrictionsCheck() {
    val context = LocalContext.current // Get the current context (Activity context)
    val unusedAppRestrictionsStatus =
        PackageManagerCompat.getUnusedAppRestrictionsStatus(context).get()
    when (unusedAppRestrictionsStatus) {
        DISABLED -> {
            // Handle when restrictions are disabled
        }

        API_30_BACKPORT -> {
            // Handle when restrictions apply on API 30-
            requestUnusedAppPermission(context)
        }

        API_30 -> {
            // Handle when restrictions apply on API 30+
            requestUnusedAppPermission(context)
        }

        API_31 -> {
            // Handle when restrictions apply on API 30+
            requestUnusedAppPermission(context)
        }

        FEATURE_NOT_AVAILABLE -> {
            // Handle unknown status

        }
    }
}}