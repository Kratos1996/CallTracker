package com.wabblaster.wabblasterai.ui.restricted

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wabblaster.wabblasterai.databinding.ActivityRestrictedContactBinding
import com.wabblaster.wabblasterai.ui.home.HomeViewModel
import com.wabblaster.wabblasterai.utils.navToContactActivity
import com.wabblaster.wabblasterai.utils.showLoadingDialog
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