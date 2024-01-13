package com.ishant.calltracker.ui.splash

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.R
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.navToLogin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
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