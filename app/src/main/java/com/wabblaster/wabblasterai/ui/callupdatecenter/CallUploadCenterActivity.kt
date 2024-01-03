package com.wabblaster.wabblasterai.ui.callupdatecenter

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wabblaster.wabblasterai.R
import com.wabblaster.wabblasterai.app.CallTrackerApplication
import com.wabblaster.wabblasterai.database.room.UploadContact
import com.wabblaster.wabblasterai.database.room.UploadContactType
import com.wabblaster.wabblasterai.databinding.ActivityCallUploadCenterBinding
import com.wabblaster.wabblasterai.ui.home.HomeViewModel
import com.wabblaster.wabblasterai.utils.showLoadingDialog
import com.wabblaster.wabblasterai.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallUploadCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallUploadCenterBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var adapter: UploadContactAdapter
    @Inject
    lateinit var progressDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCallUploadCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getUploadContactsList()
        adapter = UploadContactAdapter(this,::uploadCallNow ,::showMessage)
        binding.allRestrictedContactListRecycler.adapter = adapter
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.refresh.setOnClickListener {
            viewModel.startGettingContact (500) {
                refresh()
            }
        }
        binding.All.setOnClickListener {
            viewModel.lastApiCall = UploadContactType.ALL
            viewModel.getUploadContactsList()
            binding.All.setBackgroundResource(R.drawable.fill_main_btn)
            binding.complete.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.pending.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.All.setTextColor(resources.getColor(R.color.white))
            binding.complete.setTextColor(resources.getColor(R.color.black))
            binding.pending.setTextColor(resources.getColor(R.color.black))
        }

        binding.complete.setOnClickListener {
            viewModel.lastApiCall = UploadContactType.COMPLETE
            viewModel.getUploadContactsList(type = UploadContactType.COMPLETE)
            binding.complete.setBackgroundResource(R.drawable.fill_main_btn)
            binding.complete.setTextColor(resources.getColor(R.color.white))
            binding.All.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.pending.setTextColor(resources.getColor(R.color.black))
            binding.pending.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.All.setTextColor(resources.getColor(R.color.black))
        }

        binding.pending.setOnClickListener {
            viewModel.lastApiCall = UploadContactType.PENDING
            viewModel.getUploadContactsList(type = UploadContactType.PENDING)
            binding.pending.setBackgroundResource(R.drawable.fill_main_btn)
            binding.pending.setTextColor(resources.getColor(R.color.white))
            binding.complete.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.complete.setTextColor(resources.getColor(R.color.black))
            binding.All.setBackgroundResource(R.drawable.fill_main_white_btn)
            binding.All.setTextColor(resources.getColor(R.color.black))
        }
        lifecycleScope.launch {
            viewModel.uploadContactListMutable.collectLatest { it ->
                if (it.isNotEmpty()) {
                    binding.emptyContact.visibility = View.GONE
                    adapter.updateList(it)
                } else {
                    binding.emptyContact.visibility = View.VISIBLE
                    adapter.updateList(it)
                }
            }
        }
        viewModel.scopeMain.launch {
            viewModel.isLoading.collectLatest {
                if(it){
                    showLoadingDialog(this@CallUploadCenterActivity, progressDialog).show()
                }else{
                    showLoadingDialog(this@CallUploadCenterActivity, progressDialog).hide()
                }
            }
        }
        viewModel.scopeMain.launch {
            CallTrackerApplication.isRefreshUi.collectLatest {
                if(it){
                    refresh()
                }
            }
        }
    }

    private fun refresh() {
        when (viewModel.lastApiCall) {
            UploadContactType.ALL -> {
                viewModel.getUploadContactsList()
            }

            else -> {
                viewModel.getUploadContactsList(type = viewModel.lastApiCall)
            }
        }
    }


    private fun uploadCallNow(uploadContact: UploadContact) {
        viewModel.saveContact(uploadContact){
            CoroutineScope(Dispatchers.Main).launch {
                showMessage(it)
                refresh()
            }
        }
    }
    private fun showMessage(message: String){
        this.toast(message)
    }

}