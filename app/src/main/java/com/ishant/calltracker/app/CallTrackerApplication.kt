package com.ishant.calltracker.app
/*Android Developer
* Ishant Sharma
* Java and Kotlin
*
* */
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.ishant.calltracker.utils.AppPreference

import com.ishant.corelibcompose.toolkit.constant.AppConstants
import com.ishant.corelibcompose.toolkit.pref.ToolKitPref
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow

@HiltAndroidApp
class CallTrackerApplication  : Application(){

    companion object{
        val isRefreshUi = MutableStateFlow<Boolean>(false)
        val contactLoading = MutableStateFlow<Boolean>(false)
        val isDark = mutableStateOf(false)
    }
    override fun onCreate() {
        super.onCreate()
        AppPreference.init(this)
        ToolKitPref.init(this)
        updateAppTheme()
    }
    fun toggleAppTheme() {
        isDark.value = !isDark.value
        AppPreference.isDarkModeEnable = isDark.value
        AppPreference.isDarkMode = isDark.value
    }

    fun updateAppTheme() {
        val modeType = AppPreference.darkModeType
        isDark.value = when (modeType) {
            0 -> AppConstants.isNightModeOn(this)
            1 -> false
            2 -> true
            else -> true
        }
        AppPreference.isDarkMode = isDark.value
    }

}