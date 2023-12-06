package com.ishant.calltracker.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import com.ishant.calltracker.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

fun Context.readPhoneStatePermission(granted:()->Unit, rejected:(() -> Unit)? = null){
    val context = this
    Dexter.withContext(this)
        .withPermission(Manifest.permission.READ_PHONE_STATE)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                granted()
            }
            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                rejected?.let { it() }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest,
                permissionToken: PermissionToken
            ) {
                showCommonDialog(title = getString(R.string.required_permission),message = getString(
                    R.string.phone_state_permission),context){
                    navToSetting(context as AppCompatActivity)
                }
            }
        })
        .check()
}

fun Context.readPhoneNumberPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    val context = this
    Dexter.withContext(this)
        .withPermission(Manifest.permission.READ_PHONE_NUMBERS)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                granted()
            }
            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                if (rejected != null) {
                    rejected()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest,
                permissionToken: PermissionToken
            ) {
                showCommonDialog(title = getString(R.string.required_permission),message = getString(
                    R.string.phone_number_permission),context){
                    navToSetting(context as AppCompatActivity)
                }
            }
        })
        .check()
}

fun Context.readPhoneLogPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    val context = this
    Dexter.withContext(this)
        .withPermission(Manifest.permission.READ_CALL_LOG)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                granted()
            }
            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                rejected?.let { it() }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest,
                permissionToken: PermissionToken
            ) {
                showCommonDialog(title = getString(R.string.required_permission),message = getString(
                    R.string.read_call_log_permission),context){
                    navToSetting(context as AppCompatActivity)
                }
            }
        })
        .check()
}