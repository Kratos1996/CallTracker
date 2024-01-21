package com.ishant.calltracker.utils

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.ishant.calltracker.R
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.ui.callupdatecenter.CallUploadCenterActivity
import com.ishant.calltracker.service.CallService
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.ContactUpdateOnServer
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.ui.home.HomeActivity
import com.ishant.calltracker.ui.login.ui.login.LoginActivity
import com.ishant.calltracker.ui.logs.CallLogsActivity
import com.ishant.calltracker.ui.restricted.AddNewContact
import com.ishant.calltracker.ui.restricted.ContactActivity
import com.ishant.calltracker.ui.restricted.RestrictedContactActivity

val settingApplicationCode = 1996
val notificationId = 1
val channelId = "call_listener_channel"
fun Context.navToHome(){
    val intent = Intent(this, HomeActivity::class.java)
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
fun Context.navToCallService(){
    val intent = Intent(this, CallService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
       startService(intent)
    }
}
fun Context.navToRestrictContactActivity(){
    val intent = Intent(this, RestrictedContactActivity::class.java)
    startActivity(intent)
}
fun Context.navToContactActivity(){
    val intent = Intent(this, ContactActivity::class.java)
    startActivity(intent)
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
fun Context.serviceContact(){
    startService(Intent(this, ContactSyncService::class.java))
}
fun Context.stopServiceContactUpload(){
    stopService(Intent(this, ContactUpdateOnServer::class.java))
}
fun Context.stopServiceContact(){
    stopService(Intent(this, ContactSyncService::class.java))
}

fun Context.navToSetting(activity: AppCompatActivity){
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:" + packageName)
    )
    activity.startActivityForResult(intent,settingApplicationCode)
}

fun Context.addAutoStartup() {
    try {
        val intent = Intent()
        val manufacturer = Build.MANUFACTURER
        if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
        } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
        } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
        } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
        }else if ("Huawei".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
        }else if ("iqoo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
        }else if ("samsung".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
        }else if ("htc".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")
        }else if ("asus".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")
        }
        val list: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.size > 0 &&!(AppPreference.isRegister)) {
            AppPreference.isRegister = (true)
        }
    } catch (e: Exception) {
        AppPreference.isRegister = (false)
        Log.e("exc", e.toString())
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
    val notificationIntent = Intent(this, HomeActivity::class.java)
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
        notificationManager.notify(1996, notification)
    }
}

