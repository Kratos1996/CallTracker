package com.ishant.calltracker.ui.home

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.app.BaseObservableViewModel
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.ContactList
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.di.BaseUrlInterceptor
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.receiver.ContactObserver
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.DBResponse
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.TelephonyManagerPlus
import com.ishant.calltracker.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(
    private val app:CallTrackerApplication,
     private val  databaseRepository: DatabaseRepository,
     private var contactUseCase: ContactUseCase,
     val baseUrlInterceptor: BaseUrlInterceptor
) : BaseObservableViewModel(app) {

    var isRefreshing = mutableStateOf(false)
    @Inject
    lateinit var managerPlus: TelephonyManagerPlus
    private lateinit var autoUpdateContactObserver : ContactObserver

    val readPhoneStatePermissionGranted = mutableStateOf(false)
    val phoneNumberPermissionGranted = mutableStateOf(false)
    val phoneLogsPermissionGranted = mutableStateOf(false)
    val contactPermissionGranted = mutableStateOf(false)

    val uploadContactListMutable = MutableStateFlow<List<UploadContact>>(arrayListOf())

    val isLoading = MutableStateFlow<Boolean>(false)
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val scopeMain = CoroutineScope(Dispatchers.Main + SupervisorJob())
    var lastApiCall :String = UploadContactType.PENDING
    val contactMainList = mutableStateListOf<ContactList>()
    val contactDataFilterList = mutableStateListOf<ContactList>()
    fun loadContactObserver(context: Context){
        autoUpdateContactObserver =  ContactObserver(context , Handler())
        autoUpdateContactObserver.registerObserver()
    }
    fun getRestrictedContacts(search:String): LiveData<List<ContactList>> {
       return databaseRepository.getRestrictedDataList(search)
    }

    fun getContacts(search:String) {
       isLoading.value = true
         databaseRepository.getContactList(search).onEach { result ->
             isRefreshing.value = false
             if(result.isEmpty()){
                 isLoading.value = false
                 contactMainList.addAll(arrayListOf())
                 _errorListener.emit(Response.Message("No Records Found"))

             }else{
                 isLoading.value = false
                 contactMainList.addAll(result)
                 filterSearch()
             }
         }.launchIn(scope)
    }

    fun filterSearch() {
        try {
            val filteredList: ArrayList<ContactList> = ArrayList()
            if (contactDataFilterList != null) {
                contactDataFilterList.clear()
            }
            contactMainList.filter {
                (it.name?.lowercase()?.contains(searchString.lowercase()) == true) || (it.phoneNumber?.lowercase()?.contains(searchString.lowercase()) == true)
            }.let { contacts ->
                contacts?.let { it1 -> filteredList.addAll(it1) }
                filteredList.removeIf { callData ->
                    callData.phoneNumber.isNullOrEmpty()
                }
                contactDataFilterList.addAll(filteredList)
            }
        }catch (e:Exception){
            searchString = ""
            contactDataFilterList.clear()
        }
    }

    fun getUploadContactsList(type:String = UploadContactType.PENDING){
        scope.launch {
            databaseRepository.getUploadContactList(type).collectLatest {
                uploadContactListMutable.value = it
            }
        }
    }

    fun setRestrictedContact(phoneNumber: String, isRestricted: Boolean) {
        scope.launch {
            if(phoneNumber.isNotEmpty()) {
                databaseRepository.setRestrictedContact(phoneNumber,isRestricted)
            }
        }
    }
    fun updateUploadCall(serialNo:Long,type: String) {
        scope.launch {
            databaseRepository.updateUploadContact(serialNo,type)
        }
    }

     fun saveContact(uploadContact: UploadContact, onMessage : (String) ->Unit) {
         baseUrlInterceptor.setBaseUrl(AppPreference.baseUrl)
         contactUseCase.uploadContacts(request = Gson().fromJson(uploadContact.listOfCalls, UploadContactRequest::class.java)).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    isLoading.value = false
                    onMessage("Contact Data Not Saved on Server")
                }

                is Resource.Loading -> {
                    isLoading.value = true
                }
                is Resource.Success -> {
                    databaseRepository.deleteUploadCallData(uploadContact.serialNo)
                    isLoading.value = false
                }
            }
        }.launchIn(scope)
    }

    override fun onCleared() {
        super.onCleared()
        scope.coroutineContext.cancelChildren()
    }

    fun startGettingContact(delay:Long = 3000,isSuccess :(Boolean) ->Unit) {
        scope.launch {
            isLoading.value = true
            delay(delay)
            scopeMain.launch {
                isSuccess(true)
            }
            delay(2000)
            isLoading.value = false
        }
    }

    fun toggleAppTheme() {
        app.toggleAppTheme()
    }
}