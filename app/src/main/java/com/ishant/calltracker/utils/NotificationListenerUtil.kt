package com.ishant.calltracker.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.ishant.calltracker.service.NotificationReaderService

class NotificationListenerUtil(private val activity: Activity) {

    companion object {
        private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        private const val FAILED_TO_GRANT_TOAST = "Failed to grant Notification Listener Service permission"
    }

    fun isNotificationServiceEnabled(): Boolean {
        val componentName = ComponentName(activity, NotificationReaderService::class.java)
        val flat = Settings.Secure.getString(activity.contentResolver, ENABLED_NOTIFICATION_LISTENERS)
        return flat != null && flat.contains(componentName.flattenToString())
    }
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, NotificationReaderService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, ENABLED_NOTIFICATION_LISTENERS)

        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").map { it.trim() }
            for (name in names) {
                val enabledComponent = ComponentName.unflattenFromString(name)
                if (enabledComponent != null && enabledComponent == componentName) {
                    return true
                }
            }
        }
        return false
    }


    fun requestNotificationListenerPermission(launcher: ActivityResultLauncher<Intent>) {
        toggleNotificationListenerService(true)
        getNotificationListenerSettingsIntent()?.let { launcher.launch(it) }
    }

    private fun toggleNotificationListenerService(enable: Boolean) {
        val packageManager = activity.packageManager
        packageManager.setComponentEnabledSetting(
            ComponentName(activity, NotificationReaderService::class.java),
            if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getNotificationListenerSettingsIntent(): Intent? {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        return if (intent.resolveActivity(activity.packageManager) != null) {
            intent
        } else {
            Toast.makeText(activity, FAILED_TO_GRANT_TOAST, Toast.LENGTH_SHORT).show()
            null
        }
    }
}



