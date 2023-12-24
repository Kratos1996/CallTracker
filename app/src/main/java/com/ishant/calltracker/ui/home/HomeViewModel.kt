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
import com.ishant.calltracker.utils.Response
import com.ishant.calltracker.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(
     private val  databaseRepository: DatabaseRepository
) : AndroidViewModel(Application()) {

    val contactListMutable = MutableStateFlow<List<ContactList>>(arrayListOf())
    val isLoading = MutableStateFlow<Boolean>(false)
    val restrictedContactList = MutableStateFlow<List<ContactList>>(arrayListOf())
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

    fun setRestrictedContact(phoneNumber: String?, isRestricted: Boolean) {
        viewModelScope.launch {
            if(phoneNumber?.isNotEmpty() == true)
            databaseRepository.setRestrictedContact(phoneNumber,isRestricted)
        }
    }


}