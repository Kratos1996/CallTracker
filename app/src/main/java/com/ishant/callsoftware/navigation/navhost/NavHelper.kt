package com.ishant.callsoftware.navigation.navhost

import androidx.navigation.NavController
import com.ishant.callsoftware.app.constant.AppConst

fun navigateTo(routeName: String, navController: NavController) {
    when (routeName) {
        AppConst.BACK_CLICK_ROUTE -> {
            navController.popBackStack()
        }
        else -> {
            navController.navigate(routeName)
        }
    }
}