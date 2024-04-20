package com.ishant.calltracker.ui.navhost.screens.dashboard


sealed class AppScreenHome(val route: String) {

    data object HomeScreen : AppScreenHome("nav_home_dashboard"){
        data object DashBoardScreenHome : AppScreenHome("nav_dashboard")
        data object ContactScreen : AppScreenHome("nav_contact_screen")
        data object CallScreen : AppScreenHome("nav_call_screen")
    }
}