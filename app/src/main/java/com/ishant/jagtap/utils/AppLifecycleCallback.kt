package com.ishant.jagtap.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow


class AppLifecycleCallback : Application.ActivityLifecycleCallbacks {
    companion object {
        val lifecycleCallback = MutableStateFlow("")
    }

    private var appInBackground = true


    override fun onActivityResumed(activity: Activity) {
        lifecycleCallback.value = "Resumed"
        if (appInBackground)
            appInBackground = false
    }

    override fun onActivityPaused(activity: Activity) {
        // Schedule the lock screen action when the app goes to background
        appInBackground = true
        lifecycleCallback.value = "Paused"

    }

    // Other lifecycle callback methods...
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        lifecycleCallback.value = "Created"
    }

    override fun onActivityStarted(activity: Activity) {
        lifecycleCallback.value = "Started"
    }

    override fun onActivityStopped(activity: Activity) {
        lifecycleCallback.value = "Stopped"
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        lifecycleCallback.value = "Destroy"
    }
}
