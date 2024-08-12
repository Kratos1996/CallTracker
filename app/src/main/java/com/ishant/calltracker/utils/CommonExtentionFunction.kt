package com.ishant.calltracker.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.receiver.ServiceCheckReceiver
import com.ishant.calltracker.service.ServiceRestarterService
import com.ishant.calltracker.ui.dashboard.DashboardActivity
import com.ishant.calltracker.ui.dashboard.screens.contact.newcontact.AddNewContact
import com.ishant.calltracker.ui.login.ui.login.LoginActivity
import com.ishant.calltracker.ui.splash.SplashActivity
import com.ishant.calltracker.workmanager.ServiceCheckWorker
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

fun Context.getActivityContext(): AppCompatActivity {
    return when (this) {
        is SplashActivity -> this
        is LoginActivity -> this
        is DashboardActivity -> this
        is AddNewContact -> this
        else -> {
            this as BaseComposeActivity
        }
    }
}

fun Context.initiatePhoneCall(phoneNumber: String) {
    //val intent = Intent(Intent.ACTION_CALL)
    val intent = Intent(Intent.ACTION_DIAL)

    val uri = Uri.parse("tel:$phoneNumber")
    intent.data = uri

    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (this is Activity) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                123
            )
        }
        return
    }
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent) // This initiates the call directly

    } catch (e: Exception) {
        toast("initiatePhoneCall failed")
    }
}
fun Context.sendWhatsAppMessage(phoneNumber: String, message: String) {
    try {
        if(message.isNullOrEmpty()){
            toast("Please set your desired message to send from the dashboard")
            return
        }
        val packageManager : PackageManager =packageManager
        val isInstalled1: Boolean = isPackageInstalled("com.whatsapp", packageManager)
        val isInstalled2: Boolean = isPackageInstalled("com.whatsapp.w4b", packageManager)
        val i = Intent(Intent.ACTION_VIEW)
        val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text="+ URLEncoder.encode(message,"UTF-8")
        if(isInstalled1) {
            i.setPackage("com.whatsapp")
        }else if(isInstalled2){
            i.setPackage("com.whatsapp.w4b")
        }else{
            toast("WhatsApp is not installed")
            return
        }
        i.data = Uri.parse(url)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if(i.resolveActivity(packageManager) != null){
            startActivity(this,i,null)
        }

    } catch (e: Exception) {
        e.printStackTrace()

    }
}
private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    try {
        packageManager.getPackageInfo(packageName, 0)
        return true
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }
}
fun Context.sendSms(phoneNumber: String,msgStr:String){
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (this is Activity) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                123
            )
        }
        return
    }
    try {
        if(msgStr.isNullOrEmpty()){
            toast("Please set your desired message to send from the dashboard")
            return
        }
        val smsManager: SmsManager =
            getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(phoneNumber, null, msgStr, null, null)

        toast("SMS sent successfully")

    } catch (e: Exception) {
        e.printStackTrace()
        toast("Send SMS failed")
    }
}

fun Context.startWorkManager(viewLifecycleOwner: LifecycleOwner) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val periodicWorkRequest = PeriodicWorkRequestBuilder<ServiceCheckWorker>(
        15, TimeUnit.MINUTES
    ).setConstraints(constraints).build()
    WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
// Optional: Observe the result of the worker
    WorkManager.getInstance(applicationContext)
        .getWorkInfoByIdLiveData(periodicWorkRequest.id)
        .observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                Log.e(
                    ServiceRestarterService.TAG,
                    "CallTracker : HomeActivity > ServiceCheckWorker > doWork > CallService service is running...."
                )
            }
        })
}

fun Context.startAlarmManager() {
    // Schedule the alarm to run every minute
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, ServiceCheckReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val intervalMillis = 60 * 1000L  // 1 minute
    alarmManager.setRepeating(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + intervalMillis,
        intervalMillis,
        pendingIntent
    )
}
