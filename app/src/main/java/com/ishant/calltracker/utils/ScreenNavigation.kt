package com.ishant.calltracker.utils

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.ishant.calltracker.R
import com.ishant.calltracker.ui.callupdatecenter.CallUploadCenterActivity
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.ContactUpdateOnServer
import com.ishant.calltracker.service.KeepAliveService
import com.ishant.calltracker.ui.dashboard.DashboardActivity
import com.ishant.calltracker.ui.login.ui.login.LoginActivity
import com.ishant.calltracker.ui.logs.CallLogsActivity
import com.ishant.calltracker.ui.dashboard.screens.contact.newcontact.AddNewContact
import com.ishant.calltracker.utils.helper.AutoStartHelper.Companion.instance

val settingApplicationCode = 1996
val notificationId = 1
val channelId = "call_listener_channel"
fun Context.navToHome(){
    val intent = Intent(this, DashboardActivity::class.java)
    startActivity(intent)

}
fun Context.navToLogin(){
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
}
fun Context.navToCallLogs(uploadContactRequest:String){
    val intent = Intent(this, CallLogsActivity::class.java)
    intent.putExtra("logs", uploadContactRequest)
    startActivity(intent)
}
fun Context.navToCallService(done :()->Unit ={}){
    val intent = Intent(this, CallService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
        done()
    } else {
       startService(intent)
        done()
    }

}
fun Context.navToUploadContactActivity(){
    val intent = Intent(this, CallUploadCenterActivity::class.java)
    startActivity(intent)
}
fun Context.navToSaveContactActivity(){
    val intent = Intent(this, AddNewContact::class.java)
    startActivity(intent)
}
fun Context.serviceContactUploadRestarter(){
    /*val intent = Intent(this, ServiceRestarterService::class.java)
    startService(intent)*/

}
fun Context.keepAliveService(){
    val intent = Intent(this, KeepAliveService::class.java)
    startService(intent)

}
fun Context.serviceContact(){
    startService(Intent(this, ContactSyncService::class.java))
}
fun Context.stopServiceContactUpload(){
    stopService(Intent(this, ContactUpdateOnServer::class.java))
}
fun Context.stopServiceContact(){
    stopService(Intent(this, ContactSyncService::class.java))
}
fun Context.stopServiceCall(){
    stopService(Intent(this, CallService::class.java))
}
fun Context.stopServiceKeepAlive(){
    stopService(Intent(this, KeepAliveService::class.java))
}

fun Context.navToSetting(activity: AppCompatActivity){
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:" + packageName)
    )
    activity.startActivityForResult(intent,settingApplicationCode)
}

fun Context.addAutoStartup() {
    if(!AppPreference.isAutoStartPermissionEnabled){
        instance.getAutoStartPermission(this)
    }
}

 fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val services = activityManager?.getRunningServices(Int.MAX_VALUE)

    if (services != null) {
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    }

    return false
}

 fun Context.callForegroundService( onRunNotification: (id:Int,notification:Notification) -> Unit) {

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationIntent = Intent(this, DashboardActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        notificationIntent,
        PendingIntent.FLAG_IMMUTABLE
    )
    val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("We are Working,Please Do not force close ${getString(R.string.app_name)}")
            .setSmallIcon(R.drawable.notification_ico)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(false)
            .build()
    } else {
        NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("We are Working,Please Do not force close ${getString(R.string.app_name)}")
            .setSmallIcon(R.drawable.notification_ico)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(false)
            .build()
    }
    createNotificationChannel(notificationManager, notification,onRunNotification)

}
private fun Context.createNotificationChannel(
    notificationManager: NotificationManager,
    notification: Notification,
    onRunNotification: (id:Int,notification:Notification) -> Unit
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = resources.getString(R.string.app_name)
        val descriptionText =
            "We are Working,Please Do not force close ${getString(R.string.app_name)}"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
        onRunNotification(notificationId,notification)
    } else {
        onRunNotification(notificationId,notification)
        notificationManager.notify(notificationId, notification)
    }
}

