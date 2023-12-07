package com.ishant.calltracker.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.databinding.ActivityLoginBinding
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.TelephonyManagerPlus
import com.ishant.calltracker.utils.dataclassesUtils.TelePhoneManager
import com.ishant.calltracker.utils.readPhoneLogPermission
import com.ishant.calltracker.utils.readPhoneNumberPermission
import com.ishant.calltracker.utils.readPhoneStatePermission
import com.ishant.calltracker.utils.showCommonDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var managerPlus: TelephonyManagerPlus
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.phoneStatePermission.setOnClickListener {
            takeCallLogsPermission()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        takeCallLogsPermission()
    }

    private fun takePhoneNetworkPermission() {
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
                Log.e("CallTracker", "CallTracker : isDualSim : ${managerPlus.isDualSim}")
                Log.e("CallTracker : simOpertorName", "${managerPlus.simOperatorName1}")
                Log.e(
                    "CallTracker : simSerialNumber",
                    "${Gson().toJson(managerPlus.mSubscriptionInfoList)}"
                )
                Log.e("CallTracker : simCidNumber", "${managerPlus.cid1}")
                val data = managerPlus.getSimCardPhoneNumbers(this)
                binding.phoneStatePermission.visibility = View.GONE
                if(!data.isNullOrEmpty()) {
                    if (data.isEmpty()) {
                        binding.simEmptyView.visibility = View.VISIBLE
                        binding.dualSimUi.visibility = View.GONE
                        binding.singleSimUi.visibility = View.GONE
                    } else if (data.size == 1) {
                        binding.simEmptyView.visibility = View.GONE
                        binding.dualSimUi.visibility = View.GONE
                        binding.singleSimUi.visibility = View.VISIBLE
                        binding.careerName.text = "Carrier : ${data[0].carrierName} "
                        binding.simNumber.text = "SIM No  : ${data[0].phoneNumber} "
                    } else {
                        binding.simEmptyView.visibility = View.GONE
                        binding.dualSimUi.visibility = View.VISIBLE
                        binding.singleSimUi.visibility = View.GONE
                        binding.sim1careerName.text = "${data[0].carrierName}"
                        binding.sim1Number.text = "${data[0].phoneNumber}"
                        binding.sim2careerName.text = " ${data[1].carrierName}"
                        binding.sim2Number.text = "${data[1].phoneNumber}"
                        if (AppPreference.isSim1Selected) {
                            binding.sim1Image.setImageResource(R.drawable.sim_card_selected)
                        } else {
                            binding.sim1Image.setImageResource(R.drawable.sim_card_not_seletced)
                        }
                        if (AppPreference.isSim2Selected) {
                            binding.sim2Image.setImageResource(R.drawable.sim_card_selected)
                        } else {
                            binding.sim2Image.setImageResource(R.drawable.sim_card_not_seletced)
                        }
                        binding.sim1Image.setOnClickListener {
                            if (AppPreference.isSim1Selected) {
                                AppPreference.isSim1Selected = false
                                binding.sim1Image.setImageResource(R.drawable.sim_card_not_seletced)
                            } else {
                                AppPreference.isSim1Selected = true
                                binding.sim1Image.setImageResource(R.drawable.sim_card_selected)
                            }
                        }

                        binding.sim2Image.setOnClickListener {
                            if (!AppPreference.isSim2Selected) {
                                AppPreference.isSim2Selected = true
                                binding.sim2Image.setImageResource(R.drawable.sim_card_selected)
                            } else {
                                AppPreference.isSim2Selected = false
                                binding.sim2Image.setImageResource(R.drawable.sim_card_not_seletced)
                            }
                        }
                    }
                }else{
                    Log.e("CallTracker ", "telemanager is empty")
                }
                AppPreference.simManager = TelePhoneManager(data)
                val intent = Intent(this, CallService::class.java)
                startService(intent)
            }) {
                binding.phoneStatePermission.visibility = View.VISIBLE
            }

           // startService()

        }) {
            binding.phoneStatePermission.visibility = View.VISIBLE
        }
    }
    private fun takeCallLogsPermission(){
        readPhoneLogPermission(granted = {
            binding.phoneCallLogsPermission.visibility = View.GONE
            takePhoneNetworkPermission()
        }){
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
    }

    private fun startService() {
        requestOverLayPermissions()
    }

    private fun requestOverLayPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showCommonDialog(title = getString(R.string.required_permission),message = getString(R.string.over_lay_permission),this){
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName)
                )
                startActivityForResult(intent, 1996)
            }


        } else {
            val intent = Intent(this, CallService::class.java)
            startService(intent)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // results of permission checks
        when (requestCode) {
            1996 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        val intent = Intent(this, CallService::class.java)
                        startService(intent)
                    }

                }
            }

            else -> {
            }
        }
    }
}