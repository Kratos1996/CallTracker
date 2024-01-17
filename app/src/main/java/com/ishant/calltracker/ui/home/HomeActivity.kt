package com.ishant.calltracker.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.receiver.ContactObserver
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.TelephonyManagerPlus
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.dataclassesUtils.TelePhoneManager
import com.ishant.calltracker.utils.navToCallService
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.navToRestrictContactActivity
import com.ishant.calltracker.utils.navToUploadContactActivity
import com.ishant.calltracker.utils.serviceContactUploadRestarter
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import readPhoneContactPermission
import readPhoneLogPermission
import readPhoneNumberPermission
import readPhoneStatePermission
import requestNotificationPermission
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var managerPlus: TelephonyManagerPlus
    private lateinit var binding: ActivityHomeBinding
    private lateinit var autoUpdateContactObserver : ContactObserver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        autoUpdateContactObserver =  ContactObserver(this , Handler())
        autoUpdateContactObserver.registerObserver()
        addAutoStartup()
        binding.name.text = AppPreference.user.name?:""
        binding.emailName.text = AppPreference.user.email?:""
        binding.number.text = AppPreference.user.mobile?:""
        binding.phoneStatePermission.setOnClickListener {
            takeCallLogsPermission()
        }
        binding.phoneCallLogsPermission.setOnClickListener {
            takeCallLogsPermission()
        }
        binding.url.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url)))
            startActivity(browserIntent)
        }
    }

    override fun onStop() {
        super.onStop()
        autoUpdateContactObserver.unregisterObserver()
    }

    override fun onResume() {
        super.onResume()
        takeCallLogsPermission()
    }

    private fun takePhoneNetworkPermission() {
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                binding.uploadCallonApi.visibility = View.VISIBLE
                binding.addToRestrictedBtn.visibility = View.VISIBLE
                loadUi()
                if(!isNotificationPermissionGranted(this)) {
                    requestNotificationPermission(this)
                }else{
                    navToCallService()
                }

            }) {
                binding.phoneCallLogsPermission.visibility = View.VISIBLE

            }
        }) {
            binding.phoneStatePermission.visibility = View.VISIBLE
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
    }
    private fun loadUi(){
        val data = managerPlus.getSimCardPhoneNumbers(this)
        binding.phoneStatePermission.visibility = View.GONE
        if(!data.isNullOrEmpty()) {
            if (data.isEmpty()) {
                binding.simEmptyView.visibility = View.VISIBLE
                binding.dualSimUi.visibility = View.GONE
                binding.singleSimUi.visibility = View.GONE
            }
        }
        else{
            binding.phoneStatePermission.visibility = View.VISIBLE
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
        AppPreference.simManager = TelePhoneManager(data)
        binding.addToRestrictedBtn.setOnClickListener {
            navToRestrictContactActivity()
        }
        binding.uploadCallonApi.setOnClickListener {
            serviceContactUploadRestarter()
            navToUploadContactActivity()
        }
    }

    private fun takeCallLogsPermission(){
        readPhoneContactPermission(
            granted = {
                //serviceContact()
                readPhoneLogPermission(granted = {
                    binding.phoneCallLogsPermission.visibility = View.GONE
                    takePhoneNetworkPermission()
                }){

                    binding.phoneCallLogsPermission.visibility = View.VISIBLE
                }
            }
        ){
            binding.addToRestrictedBtn.visibility = View.GONE
            Toast.makeText(this,"Need Contact Permission",Toast.LENGTH_SHORT).show()
        }

    }

}