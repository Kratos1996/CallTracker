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
import kotlinx.coroutines.launch


class HomeViewModel() : AndroidViewModel(Application()) {

    fun loadContact(activity: Activity) {
      viewModelScope.launch {
            val resolver: ContentResolver = activity.contentResolver
            val cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null
            )

            if (cursor != null) {
                val mobileNoSet = HashSet<String>()
                try {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                    var name: String
                    var number: String
                    while (cursor.moveToNext()) {
                        val hasPhoneNumber =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                .toInt()
                        val id =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        if (hasPhoneNumber > 0) {
                            name = cursor.getString(nameIndex)
                            val phoneCursor: Cursor = activity.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf<String>(id),
                                null
                            )!!
                            if (phoneCursor.moveToNext()) {
                                val phoneNumber = phoneCursor.getString(
                                    phoneCursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    )
                                )
                                //number = deleteCountry(phoneNumber)
                                number = phoneNumber.replace("\\s+".toRegex(), "")

                                if (!mobileNoSet.contains(number)) {
                                    mobileNoSet.add(number)
                                 //save contact
                                    Log.d(
                                        "hvy", "onCreaterrView  Phone Number: name = " + name
                                                + " No = " + number
                                    )
                                }

                                //At here You can add phoneNUmber and Name to you listView ,ModelClass,Recyclerview
                                phoneCursor.close()
                            }

                        }

                    }
                } finally {
                    cursor.close()
                }
            }


        }
    }

    fun deleteCountry(phone: String): String {
        val phoneInstance = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = phoneInstance.parse(phone, "+91")
            return phoneNumber?.nationalNumber?.toString() ?: "0"
        } catch (_: Exception) {
        }
        return phone
    }

}