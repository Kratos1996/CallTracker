package com.ishant.calltracker.ui.restricted

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.databinding.ContactActivityBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.utils.DBResponse
import com.ishant.calltracker.utils.navToSaveContactActivity
import com.ishant.calltracker.utils.serviceContact
import com.ishant.calltracker.utils.showLoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: ContactActivityBinding
    private lateinit var adapter: ContactAdapter
    private var searchStringData  =""
    @Inject
    lateinit var progressDialog: Dialog
    private var  isFirstTime = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ContactActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ContactAdapter(this, 1, viewModel)
        binding.ContactListRecycler.adapter = adapter
        binding.backBtn.setOnClickListener {
            finish()
        }
       // observer(searchStringData)
        binding.addContact.setOnClickListener {
            navToSaveContactActivity()
        }

        binding.search.addTextChangedListener { search ->
            when {
                search.isNullOrEmpty() || search.isBlank() -> {
                    searchContact("")
                    searchStringData = ""
                }

                search.isNotEmpty() -> {
                    searchStringData = search.toString()
                    searchContact(search.toString())
                }
            }
        }
        binding.refresh.setOnClickListener {
            serviceContact()
        }
     /*   observer()*/

    }

    override fun onResume() {
        super.onResume()
        searchContact(searchStringData)
    }

    private fun searchContact(searchString: String = "") {
        viewModel.getContacts(searchString)
    }
  /*  private fun observer(){
        lifecycleScope.launch {
            viewModel.contactListMutable.collectLatest {result ->
                when(result){
                    DBResponse.Empty -> {
                        binding.emptyContact.text = "Your Contacts is Loading..."
                        binding.emptyContact.visibility = View.VISIBLE
                    }
                    is DBResponse.Loading -> {
                        showLoadingDialog(this@ContactActivity, progressDialog).show()
                    }
                    is DBResponse.Message -> {
                        showLoadingDialog(this@ContactActivity, progressDialog).hide()
                        binding.emptyContact.text = result.message
                        binding.emptyContact.visibility = View.VISIBLE
                        adapter.updateList(arrayListOf())
                        if(isFirstTime){
                            binding.emptyContact.text = "Your Contacts is Loading..."
                            isFirstTime = false
                            serviceContact()
                        }
                    }
                    is DBResponse.Success -> {
                        isFirstTime = false
                        binding.emptyContact.visibility = View.GONE
                        showLoadingDialog(this@ContactActivity, progressDialog).hide()
                        adapter.updateList(result.response?: arrayListOf())
                    }
                }

            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            CallTrackerApplication.contactLoading.collectLatest {
                if(it){
                    showLoadingDialog(this@ContactActivity, progressDialog).show()
                }else{
                    searchContact(searchStringData)
                    showLoadingDialog(this@ContactActivity, progressDialog).hide()
                }
            }
        }

    }*/
}