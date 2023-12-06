package com.ishant.calltracker.app
/*Android Developer
* Ishant Sharma
* Java and Kotlin
*
* */
import android.app.Application
import com.ishant.calltracker.utils.AppPreference
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CallTrackerApplication  : Application(){

    override fun onCreate() {
        super.onCreate()
        AppPreference.init(this)
    }

}