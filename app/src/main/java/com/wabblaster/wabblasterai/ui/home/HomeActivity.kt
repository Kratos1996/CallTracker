package com.wabblaster.wabblasterai.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wabblaster.wabblasterai.R
import com.wabblaster.wabblasterai.databinding.ActivityHomeBinding
import com.wabblaster.wabblasterai.receiver.ContactObserver
import com.wabblaster.wabblasterai.utils.AppPreference
import com.wabblaster.wabblasterai.utils.TelephonyManagerPlus
import com.wabblaster.wabblasterai.utils.addAutoStartup
import com.wabblaster.wabblasterai.utils.dataclassesUtils.TelePhoneManager
import com.wabblaster.wabblasterai.utils.navToRestrictContactActivity
import com.wabblaster.wabblasterai.utils.navToUploadContactActivity
import com.wabblaster.wabblasterai.utils.readPhoneContactPermission
import com.wabblaster.wabblasterai.utils.readPhoneLogPermission
import com.wabblaster.wabblasterai.utils.readPhoneNumberPermission
import com.wabblaster.wabblasterai.utils.readPhoneStatePermission
import com.wabblaster.wabblasterai.utils.serviceContact
import com.wabblaster.wabblasterai.utils.serviceContactUploadRestarter
import dagger.hilt.android.AndroidEntryPoint
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
               serviceContact()
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
                serviceContact()
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