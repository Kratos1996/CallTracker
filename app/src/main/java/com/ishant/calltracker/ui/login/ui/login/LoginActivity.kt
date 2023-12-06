package com.ishant.calltracker.ui.login.ui.login

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.databinding.ActivityLoginBinding
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.navToHome
import com.ishant.calltracker.utils.showLoadingDialog
import com.ishant.calltracker.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()
    lateinit var context: Context
    @Inject
    lateinit var progressDialog:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginBtn.setOnClickListener {
            validateNow(binding)
        }
       lifecycleScope.launch {
           loginViewModel.loginResponse.collectLatest {
               when(it){
                   Response.Empty -> {

                   }
                   is Response.Loading -> {
                       showLoadingDialog(context, progressDialog).show()
                   }
                   is Response.Message -> {
                       showLoadingDialog(context, progressDialog).hide()
                       if(it.message?.isNotEmpty() == true) {
                           context.toast(message = it.message)
                       }
                   }
                   is Response.Success ->{
                       showLoadingDialog(context, progressDialog).hide()
                       navToHome()
                   }
               }
           }
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

            else -> {
                binding.layEmail.isErrorEnabled = false
                binding.layPassword.isErrorEnabled = false
                loginViewModel.login(binding.username.text.toString(),binding.password.text.toString())
            }
        }
    }
}