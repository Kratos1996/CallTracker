package com.ishant.calltracker.ui.restricted

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.databinding.ActivityAddNewContactBinding
import com.ishant.calltracker.service.ContactSyncService
import com.ishant.calltracker.utils.ContactSaver
import com.ishant.calltracker.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddNewContact : AppCompatActivity() {
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
            }
        }
    }
}