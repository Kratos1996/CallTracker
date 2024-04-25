package com.ishant.calltracker.ui.dashboard.screens.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.ishant.calltracker.ui.dashboard.screens.common.DashboardCommon.TitleSeparator
import com.ishant.calltracker.ui.dashboard.HomeViewModel
import com.ishant.calltracker.ui.navhost.host.dashboard.HomeNavConstants
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.isServiceRunning
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.toast
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.ui.clickables.bounceClick
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.ExtraLargeText
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
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
private fun LoadDashboardScreen(context: Context, homeViewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.white)
            .verticalScroll(scrollState)
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
                RegularText.Medium(title = AppPreference.user.name ?: "")
                RegularText.Medium(title = AppPreference.user.email ?: "")
                RegularText.Medium(title = AppPreference.user.mobile ?: "")
                RegularText.Underlined(title = context.getString(R.string.url) ?: "", modifier = Modifier.noRippleClickable {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url)))
                    context.startActivity(browserIntent)
                })
            }
        }

        DashboardCardComponents(
            title = context.getString(R.string.service),
            modifier = Modifier,
            firstBlockText = context.getString(R.string.ristricted_contact),
            secondBlockText = context.getString(R.string.start_call_service),
            firstBlock = {
                homeViewModel.ristrictedContact.value = true
                homeViewModel.allContactSelected.value = false
                homeViewModel.filterSearch()
                homeViewModel.navigateTo(navCode = HomeNavConstants.NAV_ALL_CONTACTS_SCREEN)
            },
            secondBlock = {
                if (!homeViewModel.contactPermissionGranted.value && !homeViewModel.phoneLogsPermissionGranted.value && !homeViewModel.readPhoneStatePermissionGranted.value && !homeViewModel.phoneNumberPermissionGranted.value) {
                    context.toast(context.getString(R.string.please_check_all_pending_permission_for_call_service_all_permission_is_required))
                } else {
                    if (context.isServiceRunning(CallService::class.java)) {
                        context.toast("Call Service is already Running")
                    } else {
                        context.toast("Call Service started....")
                        context.navToCallService()
                    }
                }
            },
            subSecondBlockText = if (!context.isServiceRunning(CallService::class.java)) {
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
            ExtraLargeText.Medium(
                title = firstBlockText,
                textColor = MaterialTheme.colors.white_only
            )
            SubHeadingText(
                title = subFirstBlockText,
                textColor = MaterialTheme.colors.white_only
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
            ExtraLargeText.Medium(
                title = secondBlockText,
                textColor = MaterialTheme.colors.white_only
            )
            SubHeadingText(
                title = subSecondBlockText,
                textColor = MaterialTheme.colors.white_only
            )
        }

    }
}
