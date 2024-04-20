package com.ishant.calltracker.ui.dashboard.screens.call

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.app.BaseObservableViewModel
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Response
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
    var searchString = ""
    var isRefreshing = mutableStateOf(false)


    fun getCallDetails() {
        contactUseCase.getCallDetails().onEach { response ->
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

}