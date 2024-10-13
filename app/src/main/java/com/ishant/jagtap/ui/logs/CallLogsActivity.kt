package com.ishant.jagtap.ui.logs

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.gson.Gson
import com.ishant.jagtap.api.request.UploadContactRequest
import com.ishant.jagtap.databinding.ActivityCallLogsBinding
import com.ishant.jagtap.ui.dashboard.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
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