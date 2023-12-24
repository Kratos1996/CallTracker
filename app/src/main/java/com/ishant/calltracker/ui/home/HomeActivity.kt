package com.ishant.calltracker.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.TelephonyManagerPlus
import com.ishant.calltracker.utils.addAutoStartup
import com.ishant.calltracker.utils.dataclassesUtils.TelePhoneManager
import com.ishant.calltracker.utils.navToRestrictContactActivity
import com.ishant.calltracker.utils.readPhoneContactPermission
import com.ishant.calltracker.utils.readPhoneLogPermission
import com.ishant.calltracker.utils.readPhoneNumberPermission
import com.ishant.calltracker.utils.readPhoneStatePermission
import com.ishant.calltracker.utils.showCommonDialog
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

    override fun onResume() {
        super.onResume()
        takeCallLogsPermission()
    }

    private fun takePhoneNetworkPermission() {
        readPhoneStatePermission(granted = {
            readPhoneNumberPermission(granted = {
             loadUi()
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
            } else  {
               /* binding.simEmptyView.visibility = View.GONE
                binding.dualSimUi.visibility = View.GONE
                binding.singleSimUi.visibility = View.VISIBLE
                binding.careerName.text = "Carrier : ${data[0].carrierName} "
                binding.simNumber.text = "SIM No  : ${data[0].phoneNumber} "*/
            }
            /*else {
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
            }*/
        }
        else{
            Log.e("CallTracker ", "telemanager is empty")
            binding.phoneStatePermission.visibility = View.VISIBLE
            binding.phoneCallLogsPermission.visibility = View.VISIBLE
        }
        AppPreference.simManager = TelePhoneManager(data)
        /*val intent = Intent(this, CallService::class.java)
        startService(intent)*/
        binding.addToRestrictedBtn.setOnClickListener {
            navToRestrictContactActivity()
        }
    }

    private fun takeCallLogsPermission(){
        readPhoneContactPermission(
            granted = {
                binding.addToRestrictedBtn.visibility = View.VISIBLE
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