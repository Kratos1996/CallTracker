package com.ishant.calltracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ishant.calltracker.ui.home.CallService
import com.ishant.calltracker.ui.home.HomeActivity
import com.ishant.calltracker.ui.login.ui.login.LoginActivity

val settingApplicationCode = 1996
fun Context.navToHome(){
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)
}
fun Context.navToLogin(){
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
}
fun Context.navToCallService(){
    val intent = Intent(this, CallService::class.java)
    startService(intent)
}

fun Context.navToSetting(activity: AppCompatActivity){
    val intent = Intent(
        Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
        Uri.parse("package:" + packageName)
    )
    activity.startActivityForResult(intent,settingApplicationCode)
}