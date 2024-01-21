package com.ishant.calltracker.ui.logs

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.ishant.calltracker.R
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.databinding.ActivityCallLogsBinding
import com.ishant.calltracker.databinding.ActivityCallUploadCenterBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.utils.showLoadingDialog
import com.ishant.calltracker.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallLogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallLogsBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var adapter: CallLogsAdapter
    @Inject
    lateinit var progressDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receivedData = intent.getStringExtra("logs")
        val data :UploadContactRequest = Gson().fromJson(receivedData,UploadContactRequest::class.java)
        binding= ActivityCallLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getUploadContactsList()
        adapter = CallLogsAdapter(this)
        if(data.data.isNotEmpty()){
            adapter.updateList(data.data)
            binding.emptyContact.visibility = View.GONE
        }else{
            binding.emptyContact.visibility = View.VISIBLE
        }

        binding.callLogsRecycler.adapter = adapter
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

}