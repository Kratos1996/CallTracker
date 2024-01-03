package com.wabblaster.wabblasterai.ui.restricted

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.wabblaster.wabblasterai.databinding.ContactActivityBinding
import com.wabblaster.wabblasterai.ui.home.HomeViewModel
import com.wabblaster.wabblasterai.utils.navToSaveContactActivity
import com.wabblaster.wabblasterai.utils.serviceContact
import com.wabblaster.wabblasterai.utils.showLoadingDialog
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ContactActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ContactAdapter(this, 1, viewModel)
        binding.ContactListRecycler.adapter = adapter
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.addContact.setOnClickListener {
            navToSaveContactActivity()
        }

        binding.search.addTextChangedListener { search ->
            when {
                search.isNullOrEmpty() || search.isBlank() -> {
                    observer("")
                    searchStringData = ""
                }

                search.isNotEmpty() -> {
                    searchStringData = search.toString()
                    observer(search.toString())
                }
            }
        }
        binding.refresh.setOnClickListener {
            serviceContact()
            viewModel.startGettingContact {
                observer(searchStringData)
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.isLoading.collectLatest {
                if(it){
                    showLoadingDialog(this@ContactActivity, progressDialog).show()
                }else{
                    showLoadingDialog(this@ContactActivity, progressDialog).hide()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observer(searchStringData)
    }

    private fun observer(searchString: String = "") {
        viewModel.getContacts(searchString).observe(this) { it ->
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