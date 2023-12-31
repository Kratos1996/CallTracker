package com.ishant.calltracker.ui.home

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.database.room.ContactList
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(
     private val  databaseRepository: DatabaseRepository,
     private var contactUseCase: ContactUseCase
) : AndroidViewModel(Application()) {

    val uploadContactListMutable = MutableStateFlow<List<UploadContact>>(arrayListOf())
    val isLoading = MutableStateFlow<Boolean>(false)
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val scopeMain = CoroutineScope(Dispatchers.Main + SupervisorJob())
    var lastApiCall :String = UploadContactType.ALL

    fun getRestrictedContacts(search:String): LiveData<List<ContactList>> {
       return databaseRepository.getRestrictedDataList(search)
    }

    fun getContacts(search:String): LiveData<List<ContactList>> {
        return databaseRepository.getContactList(search)
    }

    fun getUploadContactsList(type:String = UploadContactType.ALL){
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
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(uploadContact.sourceMobileNo),
            mobile = Utils.extractLast10Digits(uploadContact.mobile),
            name = /*AppPreference.user.name ?: ""*/uploadContact.name,
            type = uploadContact.type).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    isLoading.value = false
                    onMessage("Contact Data Not Saved on Server")
                }

                is Resource.Loading -> {
                    isLoading.value = true
                }
                is Resource.Success -> {
                    updateUploadCall(uploadContact.serialNo,UploadContactType.COMPLETE)
                    isLoading.value = false
                }
            }
        }.launchIn(scope)
    }

    override fun onCleared() {
        super.onCleared()
        scope.coroutineContext.cancelChildren()
    }


}