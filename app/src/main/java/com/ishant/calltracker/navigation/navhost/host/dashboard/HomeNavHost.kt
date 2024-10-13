package com.ishant.calltracker.navigation.navhost.host.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.compose.rememberNavController
import com.ishant.calltracker.R
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.app.constant.AppConst
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.ui.dashboard.HomeViewModel
import com.ishant.calltracker.navigation.navhost.navigateTo
import com.ishant.calltracker.navigation.navhost.screens.dashboard.AppScreenHome
import com.ishant.calltracker.receiver.BootReceiver
import com.ishant.calltracker.receiver.NotificationServiceRestartReceiver
import com.ishant.calltracker.receiver.PhoneCallReceiver
import com.ishant.calltracker.receiver.ServiceCheckReceiver
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.keepAliveService
import com.ishant.calltracker.utils.stopServiceCall
import com.ishant.calltracker.utils.stopServiceContact
import com.ishant.corelibcompose.toolkit.colors.black_60
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.ui.clickables.bounceClick
import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.progressindicator.ProgressDialog
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.theme.OneDayCartToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeNavHost(
    startDestination: String = AppScreenHome.HomeScreen.DashBoardScreenHome.route,
    homeViewModel: HomeViewModel,
    onNavigate: (nav: Int) -> Unit
) {

    val context = LocalContext.current
    val navController = rememberNavController()
    val bottomBarVisibility = remember { mutableStateOf(false) }
    LaunchedEffect(Unit, block = {
        launch {
            homeViewModel.navigationListener.collectLatest { navigateTo ->
                when (navigateTo.navCode) {
                    HomeNavConstants.NAV_DASHBOARD_SCREEN -> {
                        navController.navigate(AppScreenHome.HomeScreen.DashBoardScreenHome.route) {
                            popUpTo(AppScreenHome.HomeScreen.DashBoardScreenHome.route) {
                                inclusive = true
                            }
                        }
                    }

                    HomeNavConstants.NAV_ALL_CONTACTS_SCREEN -> {
                        navController.navigate(AppScreenHome.HomeScreen.ContactScreen.route)
                    }

                    HomeNavConstants.NAV_CALL_SCREEN -> {
                        navController.navigate(AppScreenHome.HomeScreen.CallScreen.route)
                    }
                    HomeNavConstants.NAV_SMS_SCREEN -> {
                        navController.navigate(AppScreenHome.HomeScreen.SendSmsScreen.route)
                    }

                    AppConst.NAV_BACK_CLICK -> {
                        if (!navController.popBackStack()) {
                            context.getActivityContext().finish()
                        }else{
                            navController.popBackStack()
                        }
                    }

                    else -> {
                        onNavigate(navigateTo.navCode)
                    }
                }
            }
        }
    })

    Scaffold(
        topBar = {
            OneDayCartToolbar(
                leftIconVisible = false,
                onClickBack = {
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
                }, title = context.getString(R.string.app_name),
                rightIcon = {
                    Box(
                        modifier = Modifier
                            .bounceClick {
                                homeViewModel.toggleAppTheme()
                            }
                            .size(21.sdp)
                            .background(color = MaterialTheme.colors.black_60, shape = CircleShape)
                    ) {
                        CoreImageView.FromLocalDrawable(
                            painterResource = if (!CallTrackerApplication.isDark.value) R.drawable.icon_dark_mode else R.drawable.icon_light_mode,
                            modifier = Modifier
                                .size(14.sdp)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

            )
        },
        bottomBar = {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .background( MaterialTheme.colors.white)
            ) {
                val (bottomBar) = createRefs()
                BottomBar(
                    homeViewModel = homeViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.white)
                        .constrainAs(bottomBar) {
                            visibility
                        },
                    navController = navController,
                    dismiss = {
                        bottomBarVisibility.value = it
                    }
                )
            }
        },
        backgroundColor = MaterialTheme.colors.white

    ) {
        HomeNavGraph(
            startDestination = startDestination,
            navController = navController,
            onNavigate = { routeName ->
                navigateTo(routeName, navController)
            },
            onNavigateStack = { routeName, isPopStackId, isInclusive ->
                if (isPopStackId) {
                    navController.popBackStack(routeName, isInclusive)
                } else {
                    navigateTo(routeName, navController)
                }
            }
        )
    }

    ProgressDialog(showDialog = homeViewModel.showLoading)
}


object HomeNavConstants {
    const val NAV_DASHBOARD_SCREEN = 0
    const val NAV_ALL_CONTACTS_SCREEN = 1
    const val NAV_CALL_SCREEN = 2
    const val NAV_SMS_SCREEN = 3


}

