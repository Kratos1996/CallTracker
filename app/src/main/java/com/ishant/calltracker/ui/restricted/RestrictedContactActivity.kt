package com.ishant.calltracker.ui.restricted

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.databinding.ActivityRestrictedContactBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.ui.login.ui.login.LoginViewModel
import com.ishant.calltracker.utils.navToContactActivity
import com.ishant.calltracker.utils.showLoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RestrictedContactActivity : AppCompatActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: ActivityRestrictedContactBinding
    @Inject
    lateinit var progressDialog: Dialog
    private lateinit var adapter:ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestrictedContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ContactAdapter(this, 2, viewModel)
        binding.allRestrictedContactListRecycler.adapter = adapter
        binding.addNewRestrictedBtn.setOnClickListener {
            navToContactActivity()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        observers()
    }

    override fun onResume() {
        super.onResume()
        observers()
    }

    private fun observers() {
        val context = this
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.isLoading.collectLatest {
                if(it){
                    showLoadingDialog(context, progressDialog).show()
                }else{
                    showLoadingDialog(context, progressDialog).hide()
                }
            }
        }
        viewModel.getRestrictedContacts("").observe(this) {
            if (it.isNotEmpty()) {
                binding.emptyContact.visibility = View.GONE
                adapter.updateList(it)
            } else {
                adapter.updateList(it)
                binding.emptyContact.visibility = View.VISIBLE
            }
        }
    }
}