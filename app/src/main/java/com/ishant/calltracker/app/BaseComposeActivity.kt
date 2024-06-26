package com.ishant.calltracker.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class BaseComposeActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    fun onBackPressedWaAppBlaster(context: LifecycleOwner, onBack: () -> Unit) {

        onBackPressedDispatcher.addCallback(context, object : OnBackPressedCallback(
            enabled = true
        ) {
            override fun handleOnBackPressed() {
                onBack.invoke()
            }
        }
        )
    }
}