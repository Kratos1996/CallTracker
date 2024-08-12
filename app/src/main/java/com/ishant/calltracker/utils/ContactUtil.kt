package com.ishant.calltracker.utils

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getPhoneNumberByName(context: Context, contactName: String): String? {
    return runBlocking {
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$contactName%")

            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    do {
                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)
                        if (name.equals(contactName, ignoreCase = true)) {
                            return@withContext number
                        }
                    } while (it.moveToNext())
                }
            }
            cursor?.close()
            null
        }
    }
}

fun getPhoneNumber(): String {
    when (AppPreference.simManager.data.size) {
        1 -> {
            return if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) {
                AppPreference.loginUser.user?.mobile ?: ""
            } else {
                AppPreference.simManager.data[0].phoneNumber
            }
        }

        2 -> {
            return if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty() && AppPreference.simManager.data[1].phoneNumber.isNullOrEmpty()) {
                AppPreference.loginUser.user?.mobile ?: ""
            } else if (AppPreference.simManager.data[0].phoneNumber.isNullOrEmpty()) {
                AppPreference.loginUser.user?.mobile ?: ""
            } else {
                AppPreference.simManager.data[0].phoneNumber
            }
        }

        else -> {
            return AppPreference.loginUser.user?.mobile ?: ""
        }
    }
}

fun convertDate(date:Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val callDate = dateFormat.format(Date(date))
    return callDate
}