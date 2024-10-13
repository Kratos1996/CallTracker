package com.ishant.callsoftware.ui.dashboard.screens.call

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ishant.callsoftware.api.response.UploadCallDataRes
import com.ishant.callsoftware.api.response.getcalls.GetCallsRes
import com.ishant.callsoftware.app.BaseObservableViewModel
import com.ishant.callsoftware.app.CallTrackerApplication
import com.ishant.callsoftware.database.room.DatabaseRepository
import com.ishant.callsoftware.domain.ContactUseCase
import com.ishant.callsoftware.network.Resource
import com.ishant.callsoftware.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class CallViewModel  @Inject constructor(
    private val app:CallTrackerApplication,
     private val  databaseRepository: DatabaseRepository,
     private var contactUseCase: ContactUseCase
) : BaseObservableViewModel(app) {

    val callsDataMainList = mutableStateListOf<GetCallsRes.GetCallsData>()
    val callsDataFilterList = mutableStateListOf<GetCallsRes.GetCallsData>()
    var isRefreshing = mutableStateOf(false)
    val pendingCall = mutableStateOf(true)
    val completedCall = mutableStateOf(false)

    fun getCallDetails( ) {
        val callType = if(pendingCall.value) 0 else 1
        contactUseCase.getCallDetails(callType).onEach { response ->
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
                    callsDataMainList.clear()
                    callsDataFilterList.clear()
                    val dataList = response.data?.getCallsData?: arrayListOf()
                   /* if(dataList.isNullOrEmpty()){
                       val data=  GetCallsRes.GetCallsData(createdAt = "",
                            id = 1,
                            mobile = "7732993378",
                            name = "Ishant",
                            remark = "",
                            type = 1,
                            updatedAt = "",
                            userId = AppPreference.loginUser.user?.id
                            )
                        dataList.add(data)
                    }*/
                    callsDataMainList.addAll(dataList)
                    filterSearch()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun filterSearch() {
        try {
            val filteredList: ArrayList<GetCallsRes.GetCallsData> = ArrayList()
            if (callsDataFilterList != null) {
                callsDataFilterList.clear()
            }
            callsDataMainList.filter {
                (it.mobile?.lowercase()?.contains(searchString.lowercase()) == true) || (it.name?.lowercase()?.contains(searchString.lowercase()) == true)
            }.let { callHistory ->
                callHistory?.let { it1 -> filteredList.addAll(it1) }
                filteredList.removeIf { callData ->
                    callData.mobile.isNullOrEmpty() || callData.name.isNullOrEmpty()
                }
                callsDataFilterList.addAll(filteredList)
            }
        }catch (e:Exception){

        }
    }

    fun callDetailUpdateOnServer(data:GetCallsRes.GetCallsData, onSuccess: (isSuccess:Boolean, response:UploadCallDataRes)->Unit) {

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