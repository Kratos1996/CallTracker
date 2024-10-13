package com.ishant.callsoftware.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ishant.callsoftware.R
import com.ishant.callsoftware.app.BaseComposeActivity
import com.ishant.callsoftware.utils.AppPreference
import com.ishant.callsoftware.utils.navToHome
import com.ishant.callsoftware.utils.navToLogin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if(!AppPreference.isSim1Selected && !AppPreference.isSim2Selected){
            AppPreference.isSim1Selected = true
            AppPreference.isSim2Selected = false
        }
        lifecycleScope.launch {
            delay(1000)
           if(AppPreference.isUserLoggedIn){
               navToHome()
           }else{
               navToLogin()
           }
            finish()
        }
    }
}