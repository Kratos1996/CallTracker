package com.ishant.LKing.navigation.navhost.host.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ishant.LKing.R
import com.ishant.LKing.ui.dashboard.HomeViewModel
import com.ishant.LKing.navigation.navhost.screens.dashboard.AppScreenHome
import com.ishant.corelibcompose.toolkit.colors.text_primary
import com.ishant.corelibcompose.toolkit.colors.text_secondary
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.SmallText

@Composable
fun BottomBar(
    modifier: Modifier,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    dismiss: (hide: Boolean) -> Unit,
) {
    val goBack = BottomBarScreen.Back
    val homeItem = BottomBarScreen.Dashboard
    val callItem = BottomBarScreen.Call
    val contactItem = BottomBarScreen.Contact
    val smsItem = BottomBarScreen.Sms

    val screens = listOf(
//        goBack,
        homeItem,
        callItem,
        smsItem,
        contactItem,
    )
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = currentDestination?.route == BottomBarScreen.Back.route ||currentDestination?.route == AppScreenHome.HomeScreen.DashBoardScreenHome.route || currentDestination?.route == AppScreenHome.HomeScreen.CallScreen.route || currentDestination?.route == AppScreenHome.HomeScreen.ContactScreen.route || currentDestination?.route == AppScreenHome.HomeScreen.SendSmsScreen.route
    if (bottomBarDestination) {
        dismiss(true)
        BottomNavigation(
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.white,
            modifier = modifier
                .padding(top = 6.sdp)
                .height(56.dp)
        ) {
            screens.forEach { screen ->
                AddItemIxfiCard(
                    screen = screen,
                    currentDestination = currentDestination,
                    onClick = {
//                        if (screen.route == BottomBarScreen.Back.route) {
//                           //
//                            context.getActivityContext().showAsBottomSheet{dismiss ->
//                                CommonAlertBottomSheet(
//                                    msg = "Do you want to Logout?",
//                                    positiveText = "Yes",
//                                    onPositiveClick = {
//                                        AppPreference.logout()
//                                        context.stopServiceContact()
//                                        context.stopServiceCall()
//                                        context.keepAliveService()
//                                        context.unregisterReceiver(PhoneCallReceiver())
//                                        context.unregisterReceiver(ServiceCheckReceiver())
//                                        context.unregisterReceiver(NotificationServiceRestartReceiver())
//                                        context.unregisterReceiver(BootReceiver())
//                                        context.getActivityContext().finish()
//                                    },
//                                    negativeText = "No",
//                                    onNegativeClick = {
//                                        dismiss.invoke()
//                                    })
//                            }
//                        } else if (currentDestination?.route != screen.route) {
                            navController.navigate(screen.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = false
                                restoreState = true
                            }
                        }
                  //  }
                )
//                if (screen == BottomBarScreen.Back) {
//                    Divider(
//                        color = MaterialTheme.colors.gray_bg_dark,
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .width(1.sdp)
//                            .padding(top = 6.sdp, bottom = 8.sdp)
//                    )
//                }
            }
        }
    } else {
        dismiss(false)
    }
}

@Composable
fun RowScope.AddItemIxfiCard(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    onClick: () -> Unit
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

    BottomNavigationItem(
        modifier = Modifier.background(MaterialTheme.colors.white),
        label = {
            SmallText.Bold(
                title = screen.title,
                textColor = if (selected) MaterialTheme.colors.text_primary else MaterialTheme.colors.text_secondary
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route } == true,
        icon = {
            CoreImageView.FromLocalDrawable(
                painterResource = if (selected) screen.selectedIcon else screen.defaultIcon,
                modifier = Modifier
                    .padding(bottom = 3.sdp)
                    .size(20.sdp)
                    .wrapContentSize()
                , colorFilter = if (selected)  ColorFilter.tint(MaterialTheme.colors.text_primary) else ColorFilter.tint(MaterialTheme.colors.text_secondary)

            )
        },
        onClick = onClick
    )
}

sealed class BottomBarScreen(
    val route: String,
    var title: String,
    val selectedIcon: Int,
    val defaultIcon: Int
) {
    object Back : BottomBarScreen(
        route = "BACK",
        title = "Back",
        defaultIcon = R.drawable.back_ico_non_selected,
        selectedIcon = R.drawable.back_ico_selected
    )

    object Dashboard : BottomBarScreen(
        route = AppScreenHome.HomeScreen.DashBoardScreenHome.route,
        title = "Home",
        defaultIcon = R.drawable.home_ico_non_selected,
        selectedIcon = R.drawable.home_ico_selected
    )

    object Call : BottomBarScreen(
        route = AppScreenHome.HomeScreen.CallScreen.route,
        title = "Call",
        defaultIcon = R.drawable.call_ico_non_selected,
        selectedIcon = R.drawable.call_ico_selected
    )

    object Contact : BottomBarScreen(
        route = AppScreenHome.HomeScreen.ContactScreen.route,
        title = "Contact",
        defaultIcon = R.drawable.contact_ico_non_selected,
        selectedIcon = R.drawable.contact_ico_selected
    )
    object Sms : BottomBarScreen(
        route = AppScreenHome.HomeScreen.SendSmsScreen.route,
        title = "Sms",
        defaultIcon = R.drawable.sms_icon_non_selected,
        selectedIcon = R.drawable.sms_icon_selected
    )
}
