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
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.Utils
import com.ishant.calltracker.utils.convertDate
import com.ishant.calltracker.utils.getPhoneNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LastCallDetailsCollector(val databaseRepository: DatabaseRepository) {


    suspend fun collectLastCallDetails(context: Context): UploadContactRequest {
        val dataCaller = UploadContactRequest()
        val sortOrder = CallLog.Calls.DATE
        val callsToShow = 10
        var callsCount = 0
        val dataUploadList = ArrayList<UploadContactRequest.UploadContactData>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val bundle = Bundle()
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(sortOrder))
            bundle.putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            bundle.putString(ContentResolver.QUERY_ARG_LIMIT, "10")
            val cursor: Cursor? = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, bundle, null)
            cursor?.use { cursorData ->
                while (cursorData.moveToNext() && callsCount < callsToShow) {
                    val callerNumber = cursorData.getString(cursorData.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val callerName = getContactName(context, callerNumber)
                    val callType = getCallType(cursorData.getInt(cursorData.getColumnIndexOrThrow(CallLog.Calls.TYPE)))
                    val callDuration: Long = cursorData.getLong(cursorData.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val callDateTime: Long = cursorData.getLong(cursorData.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val dataContactDatabase = databaseRepository.getRestrictedContact(
                        phone = callerNumber,
                        isFav = true
                    )
                    Log.d("LastCallDetails", "CallTracker:  Caller Name: $callerName")
                    Log.d("LastCallDetails", "CallTracker:  Caller Number: $callerNumber")
                    Log.d("LastCallDetails", "CallTracker:  Call Type: $callType")
                    Log.d("LastCallDetails", "CallTracker:  Call Duration: $callDuration")
                    // Add more details as needed
                    // Implement your logic to save or use the call details
                    val durationFormat = String.format(
                        "%02d:%02d:%02d",
                        callDuration / 3600,
                        callDuration % 3600 / 60,
                        callDuration % 60
                    )

                    if (dataContactDatabase == null ||  dataContactDatabase.isFav == false) {
                        dataUploadList.add(
                            UploadContactRequest.UploadContactData(
                                sourceMobileNo = getPhoneNumber(),
                                mobile = callerNumber,
                                type = callType,
                                duration = durationFormat,
                                name = callerName,
                                dateTime = convertDate(callDateTime) ?: ""
                            )
                        )
                    }
                    callsCount++
                }
                dataCaller.data?.addAll(dataUploadList)
                return dataCaller
            }
            cursor?.close()
        }
        else {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 10"
            )
            cursor?.use {
                while (it.moveToNext() && callsCount < callsToShow) {
                    val callerNumber = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val callerName = getContactName(context, callerNumber)
                    val callType = getCallType(it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE)))
                    val callDuration: Long = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val callDateTime: Long = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val dataContactDatabase = databaseRepository.getRestrictedContact(
                        phone = callerName,
                        isFav = true
                    )
                    Log.d("LastCallDetails", "CallTracker:  Caller Name: $callerName")
                    Log.d("LastCallDetails", "CallTracker:  Caller Number: $callerNumber")
                    Log.d("LastCallDetails", "CallTracker:  Call Type: $callType")
                    Log.d("LastCallDetails", "CallTracker:  Call Duration: $callDuration")

                    val durationFormat = String.format(
                        "%02d:%02d:%02d",
                        callDuration / 3600,
                        callDuration % 3600 / 60,
                        callDuration % 60
                    )

                    if (dataContactDatabase == null ||  dataContactDatabase.isFav == false) {
                            dataUploadList.add(
                                UploadContactRequest.UploadContactData(
                                    sourceMobileNo = getPhoneNumber(),
                                    mobile = callerNumber,
                                    type = callType,
                                    duration = durationFormat,
                                    name = callerName,
                                    dateTime =convertDate(callDateTime) ?: ""
                                )
                            )
                        }
                    callsCount++
                }
                dataCaller.data?.addAll(dataUploadList)
                dataCaller.countryCode = AppPreference.loginUser.countryCode
                return dataCaller
            }
            cursor?.close()
        }
        return dataCaller
    }

    private fun getContactName(context: Context, phoneNumber: String): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
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
            else -> "Call Ended without Pickup"
        }
    }



}