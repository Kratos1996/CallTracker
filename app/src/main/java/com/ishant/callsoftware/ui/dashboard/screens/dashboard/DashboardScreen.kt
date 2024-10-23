package com.ishant.callsoftware.ui.dashboard.screens.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import checkPermission
import com.ishant.callsoftware.R
import com.ishant.callsoftware.api.response.WhatsappData
import com.ishant.callsoftware.app.showAsBottomSheet
import com.ishant.callsoftware.receiver.BootReceiver
import com.ishant.callsoftware.receiver.NotificationServiceRestartReceiver
import com.ishant.callsoftware.receiver.PhoneCallReceiver
import com.ishant.callsoftware.receiver.ServiceCheckReceiver
import com.ishant.callsoftware.service.CallService
import com.ishant.callsoftware.service.KeepAliveService
import com.ishant.callsoftware.ui.dashboard.HomeViewModel
import com.ishant.callsoftware.ui.dashboard.screens.common.DashboardCommon.TitleSeparator
import com.ishant.callsoftware.utils.AppLifecycleCallback
import com.ishant.callsoftware.utils.AppPreference
import com.ishant.callsoftware.utils.SimInfo
import com.ishant.callsoftware.utils.getActivityContext
import com.ishant.callsoftware.utils.isServiceRunning
import com.ishant.callsoftware.utils.keepAliveService
import com.ishant.callsoftware.utils.navToCallService
import com.ishant.callsoftware.utils.startAlarmManager
import com.ishant.callsoftware.utils.startWorkManager
import com.ishant.callsoftware.utils.stopServiceCall
import com.ishant.callsoftware.utils.stopServiceContact
import com.ishant.callsoftware.utils.toast
import com.ishant.corelibcompose.toolkit.colors.gray_bg_light
import com.ishant.corelibcompose.toolkit.colors.gray_divider
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.ui.button.CoreButton
import com.ishant.corelibcompose.toolkit.ui.checkbox.CustomCheckBox
import com.ishant.corelibcompose.toolkit.ui.clickables.bounceClick
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.PS
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
import com.ishant.corelibcompose.toolkit.ui.textstyles.SFPRO
import com.ishant.corelibcompose.toolkit.ui.textstyles.SubHeadingText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import readPostNotificationPermission
import requestNotificationPermission

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = hiltViewModel(context.getActivityContext())
    val initialApiCalled = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit, block = {
        if (!initialApiCalled.value) {
            initialApiCalled.value = true
            if( homeViewModel.notificationPermissionGranted.value) {
                if (context.isServiceRunning(CallService::class.java)) {
                    homeViewModel.callService.value = true
                } else {
                    context.toast("Call Service started....")
                    context.navToCallService()
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            AppLifecycleCallback.lifecycleCallback.collectLatest { lifecycle ->
                Log.d("TAG", "DashboardScreen: " + lifecycle)

                when (lifecycle) {

                    "Resumed" -> {
                        homeViewModel.contactPermissionGranted.value =
                            !context.checkPermission(Manifest.permission.READ_CONTACTS)
                        homeViewModel.phoneLogsPermissionGranted.value =
                            !context.checkPermission(Manifest.permission.READ_CALL_LOG)
                        homeViewModel.readPhoneStatePermissionGranted.value =
                            !context.checkPermission(Manifest.permission.READ_PHONE_STATE)
                        homeViewModel.phoneNumberPermissionGranted.value =
                            !context.checkPermission(Manifest.permission.READ_PHONE_NUMBERS)
                        homeViewModel.notificationPermissionGranted.value =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                !context.checkPermission(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                true
                            }
                        homeViewModel.getWhatsappList()
                        homeViewModel.loadSimInfo(context)
                    }

                    "Paused" -> {

                    }

                    "Stopped" -> {

                    }

                    else -> {
                    }

                }
            }
        }

    })
    LoadDashboardScreen(context, homeViewModel)

}

@Composable
private fun SimInfoList(homeViewModel: HomeViewModel, context: Context) {
    val state = rememberLazyListState()
    LazyRow(
        modifier = Modifier.wrapContentWidth(),
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(items = homeViewModel.simList) { index, item ->
            SimInfo(item, (index + 1)) {
                homeViewModel.setSelectedSim(index + 1, context)
            }
        }
    }
}

@Composable
private fun WhatsappList(homeViewModel: HomeViewModel, context: Context) {
    val state = rememberLazyListState()
    LazyRow(
        modifier = Modifier.wrapContentWidth(),
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(items = homeViewModel.whatsappList) { index, item ->
            WhatsappInfo(item, (index + 1)) {
                homeViewModel.setWhatsApp(item)
            }
        }
    }
}

@Composable
fun WhatsappInfo(whatsappData: WhatsappData, index: Int, onClick: (WhatsappData) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.sdp)
            .padding(10.sdp)
            .background(MaterialTheme.colors.gray_bg_light, shape = RoundedCornerShape(10.sdp))
            .border(
                width = 1.sdp,
                color = MaterialTheme.colors.gray_divider,
                shape = RoundedCornerShape(10.sdp)
            )
            .padding(horizontal = 20.sdp, vertical = 30.sdp).noRippleClickable {
                onClick(whatsappData)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CoreImageView.FromLocalDrawable(
            painterResource = whatsappData.image ?: 0,
            modifier = Modifier
                .width(45.sdp)
                .height(45.sdp)
        )
        RegularText.Medium(
            title = whatsappData.name ?: "",
            modifier = Modifier
                .padding(top = 10.sdp)
        )
        CustomCheckBox.invoke(
            isChecked = whatsappData.packageName == AppPreference.whatsappPackage,
            onChecked = {
                onClick(whatsappData)
            },
            modifier = Modifier.padding(top = 5.sdp).size(20.sdp),
            text = ""
        )
    }
}

@Composable
fun SimInfo(simInfo: SimInfo, index: Int, onClick: (SimInfo) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.sdp)
            .padding(10.sdp)
            .background(MaterialTheme.colors.gray_bg_light, shape = RoundedCornerShape(10.sdp))
            .border(
                width = 1.sdp,
                color = MaterialTheme.colors.gray_divider,
                shape = RoundedCornerShape(10.sdp)
            )
            .padding(horizontal = 20.sdp, vertical = 30.sdp).noRippleClickable {
                onClick(simInfo)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CoreImageView.FromLocalDrawable(
            painterResource = R.drawable.sim_card_selected,
            modifier = Modifier
                .width(30.sdp)
                .height(45.sdp)
        )
        RegularText.Medium(
            title = simInfo.carrierName,
            modifier = Modifier
                .padding(top = 10.sdp)
        )
        CustomCheckBox.invoke(isChecked = index == AppPreference.simSlot, onChecked = {
            onClick(simInfo)
        }, modifier = Modifier.padding(top = 5.sdp).size(20.sdp), text = "")
    }
}


@Composable
private fun LoadDashboardScreen(context: Context, homeViewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize().padding(bottom = 100.sdp)
            .background(MaterialTheme.colors.white)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.sdp, vertical = 5.sdp)
                .background(MaterialTheme.colors.white, shape = RoundedCornerShape(10.sdp))
        ) {
            CoreImageView.FromLocalDrawable(
                painterResource = R.drawable.logo_2,
                modifier = Modifier.width(100.sdp),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(vertical = 10.sdp),
                verticalArrangement = Arrangement.spacedBy(
                    space = 10.sdp, alignment = Alignment.CenterVertically
                )
            ) {
                RegularText.Medium(title = AppPreference.loginUser.user?.name ?: "")
                RegularText.Medium(title = AppPreference.loginUser.user?.email ?: "")
                RegularText.Medium(title = AppPreference.loginUser.user?.mobile ?: "")
                RegularText.Underlined(
                    title = context.getString(R.string.url) ?: "",
                    modifier = Modifier.noRippleClickable {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url)))
                        context.startActivity(browserIntent)
                    })
            }
        }

        SimInfoList(homeViewModel = homeViewModel, context = context)
        WhatsappList(homeViewModel = homeViewModel, context = context)
        DashboardCardComponents(
            title = context.getString(R.string.service),
            modifier = Modifier,
            firstBlockText = context.getString(R.string.start_call_service),
            secondBlockText = context.getString(R.string.work_manager),
            firstBlock = {
                if (!homeViewModel.contactPermissionGranted.value && !homeViewModel.phoneLogsPermissionGranted.value && !homeViewModel.readPhoneStatePermissionGranted.value && !homeViewModel.phoneNumberPermissionGranted.value) {
                    context.toast(context.getString(R.string.please_check_all_pending_permission_for_call_service_all_permission_is_required))
                } else {
                    if (context.isServiceRunning(CallService::class.java)) {
                        homeViewModel.callService.value = true
                        context.toast("Call Service is already Running")
                    } else {
                        context.toast("Call Service started....")
                        context.navToCallService()
                    }
                }
            },
            secondBlock = {
                context.readPhoneStatePermission(granted = {
                    context.readPhoneNumberPermission(granted = {
                        if (!context.isServiceRunning(KeepAliveService::class.java)) { // Replace with your service class
                            context.startWorkManager(context.getActivityContext())
                            context.startAlarmManager()
                            context.keepAliveService()
                            homeViewModel.managers.value = true
                        }
                    })
                })
            },
            subFirstBlockText = if (!homeViewModel.callService.value) {
                context.getString(R.string.not_running)
            } else context.getString(R.string.already_running),
            subSecondBlockText = if (!homeViewModel.managers.value) {
                context.getString(R.string.not_running)
            } else context.getString(R.string.already_running)
        )

        DashboardCardComponents(
            title = context.getString(R.string.permission_pending),
            modifier = Modifier,
            firstBlockText = context.getString(R.string.phone_state_permission),
            secondBlockText = context.getString(R.string.phone_number_permission),
            firstBlock = {
                if (!homeViewModel.readPhoneStatePermissionGranted.value) {
                    context.readPhoneStatePermission(granted = {
                        homeViewModel.readPhoneStatePermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.readPhoneStatePermissionGranted.value = false
                    })
                } else {
                    context.toast("Already Granted")
                }
            }, secondBlock = {
                if (!homeViewModel.phoneNumberPermissionGranted.value) {
                    context.readPhoneNumberPermission(granted = {
                        homeViewModel.phoneNumberPermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.phoneNumberPermissionGranted.value = false
                    })
                } else {
                    context.toast("Already Granted")
                }
            },
            subFirstBlockText = if (homeViewModel.readPhoneStatePermissionGranted.value) context.getString(
                R.string.granted
            ) else context.getString(R.string.denied),
            subSecondBlockText = if (homeViewModel.phoneNumberPermissionGranted.value) context.getString(
                R.string.granted
            ) else context.getString(R.string.denied)
        )

        DashboardTileView(
            firstBlockText = context.getString(R.string.phone_log),
            secondBlockText = context.getString(R.string.contact_permission),
            firstBlock = {
                if (!homeViewModel.phoneLogsPermissionGranted.value) {
                    context.readPhoneLogPermission(granted = {
                        homeViewModel.phoneLogsPermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.phoneLogsPermissionGranted.value = false
                    })
                } else {
                    context.toast("Already Granted")
                }
            }, secondBlock = {
                if (!homeViewModel.contactPermissionGranted.value) {
                    context.readPhoneContactPermission(granted = {
                        homeViewModel.contactPermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.contactPermissionGranted.value = false
                    })
                } else {
                    context.toast("Already Granted")
                }
            },
            subFirstBlockText = if (homeViewModel.phoneLogsPermissionGranted.value) context.getString(
                R.string.granted
            ) else context.getString(R.string.denied),
            subSecondBlockText = if (homeViewModel.contactPermissionGranted.value) context.getString(
                R.string.granted
            ) else context.getString(R.string.denied)
        )
        DashboardSingleTileView(
            firstBlockText = context.getString(R.string.notification),
            subFirstBlockText = if (homeViewModel.notificationPermissionGranted.value) context.getString(
                R.string.granted
            ) else context.getString(R.string.denied),
            firstBlock = {
                if (!homeViewModel.notificationPermissionGranted.value) {
                    context.readPostNotificationPermission(granted = {
                        homeViewModel.notificationPermissionGranted.value = true
                    }, rejected = {
                        Log.d("TAG", "LoadDashboardScreen: ")
                        homeViewModel.notificationPermissionGranted.value = false
                       requestNotificationPermission(context )
                    })
                }else{
                    context.toast("Already Granted")
                }
            }
        )
        /*  TitleSeparator(title = context.getString(R.string.reply_message), showArrow = false)


          CustomOutlinedTextFieldWithTrailingIcon(
              inputWrapper = homeViewModel.replyMessageTextWrapper,
              modifier = Modifier.padding(horizontal = 10.sdp, vertical = 10.sdp),
              hintText = context.getString(R.string.add_reply_message),
              trailingIcon = {},
              enabled = true,
              isDefaultMultiline = true,
              singleLine = false,

              onChanged = {
                  homeViewModel.replyMessageTextWrapper.dataValue.value = it
                  AppPreference.replyMsg = homeViewModel.replyMessageTextWrapper.dataValue.value
              },

              )
  //            CustomOutlinedTextFieldWithTrailingIcon(
  //                inputWrapper = homeViewModel.replyTimesMessageTextWrapper,
  //                modifier = Modifier.padding(horizontal = 10.sdp, vertical = 10.sdp),
  //                hintText = context.getString(R.string.add_Repeat_reply_message),
  //                trailingIcon = {},
  //                enabled = true,
  //                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
  //                isDefaultMultiline = true,
  //                onChanged = {
  //                    homeViewModel.replyTimesMessageTextWrapper.dataValue.value = it
  //                    AppPreference.autoReplyDelayDays = if(it.isNullOrEmpty())0 else it.toInt()
  //                    AppPreference.autoReplyDelay = if(it.isNullOrEmpty())0 else ((homeViewModel.replyTimesMessageTextWrapper.dataValue.value).toInt() * 24 * 60 * 60 * 1000).toLong()
  //                },
  //
  //                )

  */
        CoreImageView.FromLocalDrawable(
            painterResource = R.drawable.home, modifier = Modifier
                .fillMaxWidth()

        )




        CoreButton.ButtonRegularTextBoldNew(
            title = context.getString(R.string.logout), modifier = Modifier.fillMaxWidth().padding( 10.sdp).height(50.sdp) , background = Brush.verticalGradient(
                listOf(Color(context.resources.getColor(R.color.shadow)),Color(context.resources.getColor(R.color.shadow)))
            ),onClick = {
                context.getActivityContext().showAsBottomSheet{dismiss ->
                    CommonAlertBottomSheet(
                        msg = "Do you want to Logout?",
                        positiveText = "Yes",
                        onPositiveClick = {
                            AppPreference.logout()
                            context.stopServiceContact()
                            context.stopServiceCall()
                            context.keepAliveService()
                            context.unregisterReceiver(PhoneCallReceiver())
                            context.unregisterReceiver(ServiceCheckReceiver())
                            context.unregisterReceiver(NotificationServiceRestartReceiver())
                            context.unregisterReceiver(BootReceiver())
                            context.getActivityContext().finish()
                        },
                        negativeText = "No",
                        onNegativeClick = {
                            dismiss.invoke()
                        })
                }
            })






    }
}

@Composable
private fun DashboardCardComponents(
    title: String,
    modifier: Modifier,
    firstBlockText: String,
    secondBlockText: String,
    subFirstBlockText: String = "",
    subSecondBlockText: String = "",
    firstBlock: (() -> Unit?)? = null,
    secondBlock: (() -> Unit?)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {

        TitleSeparator(title = title, showArrow = false)
        DashboardTileView(
            firstBlockText = firstBlockText,
            secondBlockText = secondBlockText,
            firstBlock = firstBlock,
            secondBlock = secondBlock,
            subFirstBlockText = subFirstBlockText,
            subSecondBlockText = subSecondBlockText
        )
    }

}

@Composable
private fun DashboardTileView(
    firstBlockText: String,
    subFirstBlockText: String = "",
    secondBlockText: String = "",
    subSecondBlockText: String,
    firstBlock: (() -> Unit?)?,
    secondBlock: (() -> Unit?)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 110.sdp)
            .padding(horizontal = 10.sdp, vertical = 10.sdp),
        horizontalArrangement = Arrangement.spacedBy(10.sdp)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    shape = RoundedCornerShape(10.sdp),
                    elevation = 10.sdp
                )
                .paint(
                    painterResource(id = R.drawable.card_tile),
                    contentScale = ContentScale.FillBounds
                )
                .bounceClick {
                    firstBlock?.invoke()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SubHeadingText.Medium(
                title = firstBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = SFPRO
            )
            RegularText(
                title = subFirstBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = PS
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    shape = RoundedCornerShape(10.sdp),
                    elevation = 10.sdp
                )
                .paint(
                    painterResource(id = R.drawable.card_tile),
                    contentScale = ContentScale.FillBounds
                )
                .bounceClick {
                    secondBlock?.invoke()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SubHeadingText.Medium(
                title = secondBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = SFPRO
            )
            RegularText(
                title = subSecondBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = PS
            )
        }

    }
}

@Composable
private fun DashboardSingleTileView(
    firstBlockText: String,
    subFirstBlockText: String = "",
    firstBlock: (() -> Unit?)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 110.sdp)
            .padding(horizontal = 10.sdp, vertical = 10.sdp),
        horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    shape = RoundedCornerShape(10.sdp),
                    elevation = 10.sdp
                )
                .paint(
                    painterResource(id = R.drawable.card_tile),
                    contentScale = ContentScale.FillBounds
                )
                .bounceClick {
                    firstBlock?.invoke()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SubHeadingText.Medium(
                title = firstBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = SFPRO
            )
            RegularText(
                title = subFirstBlockText,
                textColor = MaterialTheme.colors.white_only,
                fontStyle = PS
            )
        }


    }
}
