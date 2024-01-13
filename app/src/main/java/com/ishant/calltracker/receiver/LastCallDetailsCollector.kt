package com.ishant.calltracker.receiver

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow


class LastCallDetailsCollector {

    data class ContactData( val callerNumber : String,val callerName :String , val callType :String)

    fun collectLastCallDetails(context: Context): MutableStateFlow<ContactData?> {
        val dataCaller = MutableStateFlow<ContactData?>(null)
        val sortOrder = CallLog.Calls.DATE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bundle = Bundle()
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(sortOrder))
            bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            bundle.putString(ContentResolver.QUERY_ARG_LIMIT, "1")

            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI, null,bundle, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val callerNumber = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val callerName = getContactName(context, callerNumber)
                    val callType = getCallType(it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE)))
                    val callDuration: String = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    Log.d("LastCallDetails", "CallTracker:  Caller Name: $callerName")
                    Log.d("LastCallDetails", "CallTracker:  Caller Number: $callerNumber")
                    Log.d("LastCallDetails", "CallTracker:  Call Type: $callType")
                    Log.d("LastCallDetails", "CallTracker:  Call Duration: $callDuration")
                    // Add more details as needed
                    // Implement your logic to save or use the call details

                    dataCaller.value = ( ContactData(callType = callType, callerName = callerName, callerNumber = callerNumber))
                    return  dataCaller
                }
        }
        }else{
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 1"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val callerNumber = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val callerName = getContactName(context, callerNumber)
                    val callType = getCallType(it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE)))

                    Log.d("LastCallDetails", "CallTracker:  Caller Name: $callerName")
                    Log.d("LastCallDetails", "CallTracker:  Caller Number: $callerNumber")
                    Log.d("LastCallDetails", "CallTracker:  Call Type: $callType")
                    // Add more details as needed
                    // Implement your logic to save or use the call details

                    dataCaller.value = ( ContactData(callType = callType, callerName = callerName, callerNumber = callerNumber))
                    return  dataCaller
                }
            }
        }

        return dataCaller
    }

    private fun getContactName(context: Context, phoneNumber: String): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return phoneNumber // Return the phone number if name not found
    }

    private fun getCallType(callTypeCode: Int): String {
        return when (callTypeCode) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            else -> "Unknown"
        }
    }
}