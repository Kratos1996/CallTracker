package com.ishant.calltracker.ui.login.ui.login

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.R
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.databinding.ActivityLoginBinding
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.ContactSaver
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.showLoadingDialog
import com.ishant.calltracker.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import isNotificationPermissionGranted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import requestNotificationPermission
import takeForegroundCallService
import takeForegroundService
import writePhoneContactPermission
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    lateinit var context: Context
    lateinit var spinnerAdapter:UrlAdapter
    var urlResponseData: List<UrlResponse.Data> = arrayListOf()
    @Inject
    lateinit var progressDialog:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginBtn.setOnClickListener {
            requestWriteContact()
        }
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

    private fun lifecycleLoginResponse() {
        lifecycleScope.launch {
            loginViewModel.domain.collectLatest { domains ->
                when (domains) {
                    Response.Empty -> {}
                    is Response.Loading -> showLoadingDialog(context, progressDialog).show()
                    is Response.Message -> {
                        showLoadingDialog(context, progressDialog).hide()
                        context.toast(message = "Not Authorize")

                    }

                    is Response.Success -> {
                        showLoadingDialog(context, progressDialog).hide()
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
                        showLoadingDialog(context, progressDialog).show()
                    }

                    is Response.Message -> {
                        showLoadingDialog(context, progressDialog).hide()
                        context.toast(message = "Credentials Mismatched,Please Check your Phone or password ")

                    }

                    is Response.Success -> {
                        showLoadingDialog(context, progressDialog).hide()
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
        writePhoneContactPermission(granted = {
            loginViewModel.permissionGrantedMain.value = true
            validateNow(binding)
        }){
            Toast.makeText(this,getString(R.string.read_write_contact_permission),Toast.LENGTH_SHORT).show()
            loginViewModel.permissionGrantedMain.value = false
        }
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
                loginViewModel.login(binding.username.text.toString(),binding.password.text.toString())
                ContactSaver.saveContact(this,name = " Wappblaster Support","917375092569")
            }
        }
    }
}