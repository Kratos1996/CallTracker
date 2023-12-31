package com.ishant.calltracker.ui.restricted

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.ishant.calltracker.R
import com.ishant.calltracker.databinding.ActivityHomeBinding
import com.ishant.calltracker.databinding.ActivityRestrictedContactBinding
import com.ishant.calltracker.databinding.ContactActivityBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.ui.login.ui.login.LoginViewModel
import com.ishant.calltracker.utils.navToSaveContactActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: ContactActivityBinding
    private lateinit var adapter: ContactAdapter
    private var searchStringData  =""
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
            observer(searchStringData)
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