package com.ishant.calltracker.ui.login.ui.login

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ishant.calltracker.R
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.app.BaseComposeActivity
import com.ishant.calltracker.databinding.ActivityLoginBinding
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.service.NotificationReaderService
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.ContactSaver
import com.ishant.calltracker.utils.NotificationListenerUtil
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import readPhoneContactPermission
import requestNotificationPermission
import takeForegroundContactService
import takeForegroundService
import writePhoneContactPermission
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseComposeActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    lateinit var context: Context
    lateinit var spinnerAdapter:UrlAdapter
    val REQUEST_CODE_ACCESS_NOTIFICATIONS = 777
    var urlResponseData: List<UrlResponse.Data> = arrayListOf()
    @Inject
    lateinit var progressDialog:Dialog
    lateinit var notificationListenerUtil: NotificationListenerUtil
    private lateinit var notificationListenerPermissionLauncher: ActivityResultLauncher<Intent>

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    } else {
                        loadHome()
                    }
                }
            } else {
                loadHome()
            }
        }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                loadHome()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                loadHome()
            } else {
                // Directly ask for the permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            loadHome()
        }
    }

    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(
            this, com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else
                    loadHome()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:com.ishant.calltracker")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadHome() {
        writePhoneContactPermission(granted = {
            takeForegroundContactService(granted = {
                readPhoneContactPermission(granted = {
                    startService(Intent(this, ContactSyncService::class.java))
                    loginViewModel.permissionGrantedMain.value = true
                    loginViewModel.login(binding.username.text.toString(), binding.password.text.toString())
                }){
                    toast("Need Read Contact Service")
                }
            })
        }){
            Toast.makeText(this,getString(R.string.read_write_contact_permission),Toast.LENGTH_SHORT).show()
            loginViewModel.permissionGrantedMain.value = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        notificationListenerPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val granted = notificationListenerUtil.isNotificationServiceEnabled()
                if(granted){
                    val mServiceIntent = Intent(this, NotificationReaderService::class.java)
                    startService(mServiceIntent)
                }
            }
        notificationListenerUtil = NotificationListenerUtil(this)
        binding.loginBtn.setOnClickListener {
            requestWriteContact()
        }
        readNotificationService()
        lifecycleScope.launch {
            loginViewModel.databaseRepository.deleteAll()
        }
        AppPreference.logout()
        val termsAndConditionsText = getString(R.string.i_accept_this_terms_condition)
        val spannableString = SpannableString(termsAndConditionsText)
        // Customize the spannable text as needed
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                loadUrl(url = getString(R.string.terms_and_condition_url))
            }
        }

        binding.createAccount.setOnClickListener {
            loadUrl(url = getString(R.string.create_account_url))
        }

        // Specify the start and end index for the clickable part of the text
        val startIndex = termsAndConditionsText.indexOf("Terms")
        val endIndex = startIndex + "Terms & Condition".length

        // Set the ClickableSpan to the specified range
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Make the TextView clickable
        binding.acceptTerms.movementMethod = LinkMovementMethod.getInstance()

        // Set the spannable text to the TextView
        binding.acceptTerms.text = spannableString
        lifecycleLoginResponse()
        binding.domainSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                binding.domains.text = urlResponseData[position].urlName
                AppPreference.baseUrl = getHostFromUrl(urlResponseData[position].urlValue?:"https://wappblaster.in/api/")?:"wappblaster.in"
                Log.e("Login","BaseUrl : ${AppPreference.baseUrl}")
                loginViewModel.baseUrlInterceptor.setBaseUrl(AppPreference.baseUrl)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun readNotificationService() {
       if(!notificationListenerUtil.isNotificationServiceEnabled()){
           notificationListenerUtil.requestNotificationListenerPermission(notificationListenerPermissionLauncher)
       }
    }

    private fun lifecycleLoginResponse() {
        lifecycleScope.launch {
            loginViewModel.domain.collectLatest { domains ->
                when (domains) {
                    Response.Empty -> {}
                    is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is Response.Message -> {
                        binding.progressBar.visibility = View.GONE
                        context.toast(message = "Not Authorize")

                    }

                    is Response.Success -> {
                        binding.progressBar.visibility = View.GONE
                        urlResponseData = domains.response?.urlResponseData ?: arrayListOf()
                        spinnerAdapter = UrlAdapter(
                            this@LoginActivity,
                            R.layout.custom_spinner,
                            urlResponseData
                        )
                        binding.domainSpinner.adapter = spinnerAdapter
                        binding.domains.setOnClickListener { binding.domainSpinner.performClick() }
                    }
                }
            }
        }
        lifecycleScope.launch {
            loginViewModel.loginResponse.collectLatest {
                when (it) {
                    Response.Empty -> {

                    }

                    is Response.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Response.Message -> {
                        binding.progressBar.visibility = View.GONE
                        context.toast(message = "Credentials Mismatched,Please Check your Phone or password ")

                    }

                    is Response.Success -> {
                        binding.progressBar.visibility = View.GONE
                        takeForegroundService(granted = {
                            if (isNotificationPermissionGranted(context)) {
                                navToHome()
                            } else {
                                requestNotificationPermission(context)
                            }
                        }) {
                            context.toast(message = "Need Permission Foreground  Service")
                        }
                    }
                }
            }
        }
    }

    fun getHostFromUrl(url: String): String? {
        return try {
            val uri = URI(url)
            uri.host
        } catch (e: URISyntaxException) {
            null
        }
    }

    private fun loadUrl(url:String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun requestWriteContact(){
        validateNow(binding)
    }

    private fun validateNow(binding: ActivityLoginBinding) {
        when {
            binding.username.text.isNullOrEmpty() -> {
                binding.layEmail.isErrorEnabled = true
                binding.layEmail.error = "Please Enter Phone Number"
            }

            binding.password.text.isNullOrEmpty() -> {
                binding.layPassword.isErrorEnabled = true
                binding.layPassword.error = "Please Enter Password"
            }
            !binding.acceptTerms.isChecked ->{
                toast("Please Accept Terms and Condition ")
            }

            else -> {
                binding.layEmail.isErrorEnabled = false
                binding.layPassword.isErrorEnabled = false
                askNotificationPermission()

                ContactSaver.saveContact(this,name = " Wappblaster Support","917375092569")
            }
        }
    }
}