package com.ishant.calltracker.ui.callupdatecenter

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.databinding.ActivityCallUploadCenterBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.utils.navToCallLogs
import com.ishant.calltracker.utils.showLoadingDialog
import com.ishant.calltracker.utils.toast
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
        adapter = UploadContactAdapter(this,::uploadCallNow ,::showMessage,::navToCallLogsActivity)
        binding.allRestrictedContactListRecycler.adapter = adapter
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.refresh.setOnClickListener {
            viewModel.startGettingContact (500) {
                refresh()
            }
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
                    CallTrackerApplication.isRefreshUi.value = false
                }
            }
        }
    }

    private fun refresh() {
        viewModel.getUploadContactsList(type = viewModel.lastApiCall)
    }


    private fun uploadCallNow(uploadContact: UploadContact) {
        viewModel.saveContact(uploadContact){
            CoroutineScope(Dispatchers.Main).launch {
                showMessage(it)
                refresh()
            }
        }
    }

    private fun navToCallLogsActivity(data:String){
        navToCallLogs(uploadContactRequest = data)
    }
    private fun showMessage(message: String){
        this.toast(message)
    }

}