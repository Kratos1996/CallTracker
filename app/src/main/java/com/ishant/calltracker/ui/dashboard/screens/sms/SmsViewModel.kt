package com.ishant.calltracker.ui.dashboard.screens.call

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ishant.calltracker.api.response.UploadCallDataRes
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.api.response.sms.SendSmsRes
import com.ishant.calltracker.app.BaseObservableViewModel
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class SmsViewModel  @Inject constructor(
    private val app:CallTrackerApplication,
     private val  databaseRepository: DatabaseRepository,
     private var contactUseCase: ContactUseCase
) : BaseObservableViewModel(app) {

    val sendSmsDataMainList = mutableStateListOf<SendSmsRes.SendSmsData>()
    val sendSmsDataFilterList = mutableStateListOf<SendSmsRes.SendSmsData>()
    var isRefreshing = mutableStateOf(false)
    val pendingSms = mutableStateOf(true)
    val completedSms = mutableStateOf(false)
    fun getSms( ) {
        val smsType = if (pendingSms.value) 0 else 1
        contactUseCase.sendSms().onEach { response ->
            when(response){
                is Resource.Error -> {
                    _errorListener.emit(Response.Message(response.message?:""))
                    showLoading.value = false
                    isRefreshing.value = false
                }
                is Resource.Loading -> {
                    showLoading.value = true
                }
                is Resource.Success -> {
                  isRefreshing.value = false
                    showLoading.value = false
                    sendSmsDataMainList.clear()
                    sendSmsDataFilterList.clear()
                    response.data?.let {
                        sendSmsDataMainList.addAll(it.sendSmsData.filter { smsType == it.status })
                    }
                    filterSearch()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun filterSearch() {
        try {
            val filteredList: ArrayList<SendSmsRes.SendSmsData> = ArrayList()
            if (sendSmsDataFilterList != null) {
                sendSmsDataFilterList.clear()
            }
            sendSmsDataMainList.filter {
                (it.mobile?.lowercase()?.contains(searchString.lowercase()) == true) || (it.name?.lowercase()?.contains(searchString.lowercase()) == true)
            }.let { smsHistory ->
                smsHistory?.let { it1 -> filteredList.addAll(it1) }
                filteredList.removeIf { callData ->
                    callData.mobile.isNullOrEmpty() || callData.name.isNullOrEmpty()
                }
                sendSmsDataFilterList.addAll(filteredList)
            }
        }catch (e:Exception){

        }
    }

    fun sendSmsDetailUpdateOnServer(data:GetCallsRes.GetCallsData, onSuccess: (isSuccess:Boolean, response:UploadCallDataRes)->Unit) {
        contactUseCase.uploadCallDetails(data).onEach { response ->
            when(response){
                is Resource.Error -> {
                    _errorListener.emit(Response.Message(response.message?:""))
                    showLoading.value = false
                }
                is Resource.Loading -> {
                    showLoading.value = true
                }
                is Resource.Success -> {
                    response.data?.let { onSuccess(true, it) }
                    showLoading.value = false
                }
            }
        }.launchIn(viewModelScope)
    }

}