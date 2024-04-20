package com.ishant.calltracker.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.ui.dashboard.DashboardActivity
import com.ishant.calltracker.ui.login.ui.login.LoginActivity
import com.ishant.calltracker.ui.splash.SplashActivity

fun Context.getActivityContext(): AppCompatActivity {
    return when (this) {
        is SplashActivity -> this
        is LoginActivity -> this
        is DashboardActivity -> this
        else -> {
            this as BaseComposeActivity
        }
    }
}