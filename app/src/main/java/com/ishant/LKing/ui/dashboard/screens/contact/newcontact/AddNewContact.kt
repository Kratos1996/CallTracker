package com.ishant.LKing.ui.dashboard.screens.contact.newcontact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ishant.LKing.app.BaseComposeActivity
import com.ishant.LKing.database.room.DatabaseRepository
import com.ishant.LKing.databinding.ActivityAddNewContactBinding
import com.ishant.LKing.service.ContactSyncService
import com.ishant.LKing.utils.ContactSaver
import com.ishant.LKing.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddNewContact : BaseComposeActivity() {
    private lateinit var binding:ActivityAddNewContactBinding
    @Inject
    lateinit var databaseRepository: DatabaseRepository
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        binding.saveContact.setOnClickListener {
            validateNow(binding = binding)
        }
        binding.backBtn.setOnClickListener {
            setResult(RESULT_CANCELED, Intent().putExtra("VALUE", 0))
            finish()
        }
        onBackPressedWaAppBlaster(this){
            setResult(RESULT_CANCELED, Intent().putExtra("VALUE", 0))
            finish()
        }
    }
    private fun validateNow(binding: ActivityAddNewContactBinding) {
        when {
            binding.mobileNumber.text.isNullOrEmpty() -> {
                binding.layEmail.isErrorEnabled = true
                binding.layEmail.error = "Please Enter Phone Number"
            }

            binding.name.text.isNullOrEmpty() -> {
                binding.layPassword.isErrorEnabled = true
                binding.layPassword.error = "Please Enter Name"
            }
            else -> {
                binding.layEmail.isErrorEnabled = false
                binding.layPassword.isErrorEnabled = false
                ContactSaver.saveContact(this,name = binding.name.text.toString(), binding.mobileNumber.text.toString())
                startService(Intent(this, ContactSyncService::class.java))
                context.toast("Contact Saved Successfully")
                binding.mobileNumber.setText("")
                binding.name.setText("")
                setResult(RESULT_OK, Intent().putExtra("VALUE", 0))
                finish()
            }
        }
    }
}