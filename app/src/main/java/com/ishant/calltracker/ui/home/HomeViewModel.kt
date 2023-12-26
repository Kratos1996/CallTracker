package com.ishant.calltracker.ui.home

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(
     private val  databaseRepository: DatabaseRepository,
     private var contactUseCase: ContactUseCase
) : AndroidViewModel(Application()) {

    val contactListMutable = MutableStateFlow<List<ContactList>>(arrayListOf())
    val uploadContactListMutable = MutableStateFlow<List<UploadContact>>(arrayListOf())
    val isLoading = MutableStateFlow<Boolean>(false)
    val restrictedContactList = MutableStateFlow<List<ContactList>>(arrayListOf())
    var lastApiCall :String = UploadContactType.ALL
    fun loadContact(activity: Activity) {
        isLoading.value = true
      viewModelScope.launch {
            val resolver: ContentResolver = activity.contentResolver
            val cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null
            )
            if (cursor != null) {
                val mobileNoSet = HashSet<String>()
                cursor.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    var name: String
                    var number: String
                    while (it.moveToNext()) {
                        val hasPhoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()
                        val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        if (hasPhoneNumber > 0) {
                            name = it.getString(nameIndex)
                            val phoneCursor: Cursor = activity.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf<String>(id), null)!!
                            if (phoneCursor.moveToNext()) {
                                val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                number = phoneNumber.replace("\\s+".toRegex(), "")
                                if (!mobileNoSet.contains(number)) {
                                    mobileNoSet.add(number)
                                   databaseRepository.insertContact(ContactList(phoneNumber =  Utils.extractLast10Digits(number),name = name ,isFav = false))
                                }
                                phoneCursor.close()
                                isLoading.value = false
                            }

                        }

                    }
                }
            }

        }
    }

    fun getRestrictedContacts(search:String){
        viewModelScope.launch {
            restrictedContactList.value = databaseRepository.getRestrictedDataList(search)
        }
    }

    fun getContacts(search:String){
        viewModelScope.launch {
            contactListMutable.value = databaseRepository.getContactList(search)
        }
    }

    fun getUploadContactsList(type:String = UploadContactType.ALL){
        viewModelScope.launch {
            uploadContactListMutable.value = databaseRepository.getUploadContactList(type)
        }
    }

    fun setRestrictedContact(phoneNumber: String, isRestricted: Boolean) {
        viewModelScope.launch {
            if(phoneNumber.isNotEmpty()) {
                databaseRepository.setRestrictedContact(phoneNumber,isRestricted)
            }
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
                    val data = UploadContact(serialNo = System.currentTimeMillis(),
                        sourceMobileNo = uploadContact.sourceMobileNo,
                        mobile = uploadContact.mobile,
                        name = uploadContact.name,
                        type = UploadContactType.PENDING,
                        apiPushed = false
                    )
                    databaseRepository.insertUpload(data)
                    isLoading.value = false
                    onMessage("Contact Data Not Saved on Server")
                }

                is Resource.Loading -> {
                    isLoading.value = true
                }
                is Resource.Success -> {
                    val data = UploadContact(
                        serialNo = System.currentTimeMillis(),
                        sourceMobileNo = uploadContact.sourceMobileNo,
                        mobile = uploadContact.mobile,
                        name = uploadContact.name,
                        type = UploadContactType.COMPLETE,
                        apiPushed = true
                    )
                    databaseRepository.insertUpload(data)
                    isLoading.value = false
                }
            }
        }.launchIn(
            CoroutineScope(Dispatchers.Default)
        )
    }


}