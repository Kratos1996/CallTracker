package com.ishant.LKing.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ishant.LKing.R
import com.ishant.LKing.app.BaseComposeActivity
import com.ishant.LKing.utils.AppPreference
import com.ishant.LKing.utils.navToHome
import com.ishant.LKing.utils.navToLogin
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