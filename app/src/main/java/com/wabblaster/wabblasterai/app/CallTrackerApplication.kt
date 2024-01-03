package com.wabblaster.wabblasterai.app
/*Android Developer
* Ishant Sharma
* Java and Kotlin
*
* */
import android.app.Application
import com.wabblaster.wabblasterai.utils.AppPreference
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow

@HiltAndroidApp
class CallTrackerApplication  : Application(){
    companion object{
        val isRefreshUi = MutableStateFlow<Boolean>(false)
        val contactLoading = MutableStateFlow<Boolean>(false)
    }
    override fun onCreate() {
        super.onCreate()
        AppPreference.init(this)
    }

}