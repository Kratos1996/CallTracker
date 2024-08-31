package com.ishant.calltracker.navigation.navhost.host.dashboard

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ishant.calltracker.app.constant.AppConst
import com.ishant.calltracker.ui.dashboard.screens.call.CallScreen
import com.ishant.calltracker.ui.dashboard.screens.contact.ContactScreen
import com.ishant.calltracker.ui.dashboard.screens.dashboard.DashboardScreen
import com.ishant.calltracker.navigation.navhost.screens.dashboard.AppScreenHome
import com.ishant.calltracker.ui.dashboard.screens.sms.SmsScreen

@Composable
fun HomeNavGraph(
    startDestination: String ,
    navController: NavHostController,
    onNavigate: (rootName: String) -> Unit,
    onNavigateStack: (rootName: String, isPopStackId: Boolean, isInclusive: Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        route = AppScreenHome.HomeScreen.route,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(AppConst.DURATION_500))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(AppConst.DURATION_500))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(AppConst.DURATION_500))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(AppConst.DURATION_500))
        }
    ) {
        composable(route = AppScreenHome.HomeScreen.DashBoardScreenHome.route) {
            DashboardScreen()
        }
        composable(route = AppScreenHome.HomeScreen.CallScreen.route) {
            CallScreen()
        }
        composable(route = AppScreenHome.HomeScreen.ContactScreen.route) {
            ContactScreen()
        }
        composable(route = AppScreenHome.HomeScreen.SendSmsScreen.route) {
            SmsScreen()
        }
    }
}
