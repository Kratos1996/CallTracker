package com.ishant.calltracker.ui.dashboard.screens.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ishant.calltracker.R
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.ui.dashboard.screens.common.DashboardCommon.TitleSeparator
import com.ishant.calltracker.ui.dashboard.HomeViewModel
import com.ishant.calltracker.database.AppPreference
import com.ishant.calltracker.utils.SimInfo
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.startAlarmManager
import com.ishant.calltracker.utils.startWorkManager
import com.ishant.calltracker.utils.toast
import com.ishant.corelibcompose.toolkit.colors.gray_bg_light
import com.ishant.corelibcompose.toolkit.colors.gray_divider
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.ui.clickables.bounceClick
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.PS
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
import com.ishant.corelibcompose.toolkit.ui.textstyles.SFPRO
import com.ishant.corelibcompose.toolkit.ui.textstyles.SubHeadingText
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = hiltViewModel(context.getActivityContext())
    val initialApiCalled = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit, block = {
        if (!initialApiCalled.value) {
            initialApiCalled.value = true
        }
    })
    LoadDashboardScreen(context, homeViewModel)

}

@Composable
private fun SimInfoList(homeViewModel: HomeViewModel){
    val state = rememberLazyListState()
    LazyRow (modifier = Modifier.wrapContentWidth(),
        state = state,
        verticalAlignment = Alignment.CenterVertically) {
        items(items = homeViewModel.simList){item->
            SimInfo(item)
        }
    }
}

@Composable
fun SimInfo(simInfo: SimInfo){
    Column (
        modifier = Modifier
        .width(150.sdp)
        .padding(10.sdp)
        .background(MaterialTheme.colors.gray_bg_light, shape = RoundedCornerShape(10.sdp))
        .border(width = 1.sdp, color = MaterialTheme.colors.gray_divider, shape = RoundedCornerShape(10.sdp))
        .padding(horizontal = 20.sdp, vertical = 30.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CoreImageView.FromLocalDrawable(painterResource= R.drawable.sim_card_selected,
            modifier = Modifier
            .width(30.sdp)
            .height(45.sdp))
        RegularText.Medium(
            title = simInfo.carrierName,
            modifier = Modifier
                .padding(top = 10.sdp)
        )
    }
}


@Composable
private fun LoadDashboardScreen(context: Context, homeViewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                RegularText.Underlined(title = context.getString(R.string.url) ?: "", modifier = Modifier.noRippleClickable {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url)))
                    context.startActivity(browserIntent)
                })
            }
        }

        SimInfoList(homeViewModel = homeViewModel)

        DashboardCardComponents(
            title = context.getString(R.string.service),
            modifier = Modifier,
            firstBlockText = context.getString(R.string.start_call_service),
            secondBlockText =   context.getString(R.string.work_manager),
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
            subFirstBlockText = if (!homeViewModel.callService.value ) {
                context.getString(R.string.not_running)
            } else context.getString(R.string.already_running),
            subSecondBlockText = if (! homeViewModel.managers.value) {
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
                }else{
                    context.toast("Already Granted")
                }
            }, secondBlock = {
                if (!homeViewModel.phoneNumberPermissionGranted.value) {
                    context.readPhoneNumberPermission(granted = {
                        homeViewModel.phoneNumberPermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.phoneNumberPermissionGranted.value = false
                    })
                }else{
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
                }else{
                    context.toast("Already Granted")
                }
            }, secondBlock = {
                if (!homeViewModel.contactPermissionGranted.value) {
                    context.readPhoneContactPermission(granted = {
                        homeViewModel.contactPermissionGranted.value = true
                    }, rejected = {
                        homeViewModel.contactPermissionGranted.value = false
                    })
                }else{
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


        CoreImageView.FromLocalDrawable(
            painterResource = R.drawable.home, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.sdp)
        )
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
