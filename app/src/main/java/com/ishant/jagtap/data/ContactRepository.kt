package com.ishant.jagtap.data

import com.ishant.jagtap.api.request.UploadContactRequest
import com.ishant.jagtap.api.response.UploadCallDataRes
import com.ishant.jagtap.api.response.ContactSavedResponse
import com.ishant.jagtap.api.response.LoginResponse
import com.ishant.jagtap.api.response.UploadContactResponse
import com.ishant.jagtap.api.response.UrlResponse
import com.ishant.jagtap.api.response.getcalls.GetCallsRes
import com.ishant.jagtap.api.response.sms.SendSmsRes
import okhttp3.RequestBody


interface ContactRepository {
    suspend fun loginNow(mobile:String, password:String ): LoginResponse
    suspend fun uploadContact(data: UploadContactRequest ): ContactSavedResponse
    suspend fun uploadContacts(request : UploadContactRequest): UploadContactResponse
    suspend fun getDomains(): UrlResponse
    suspend fun getCallDetails(callType:Int): GetCallsRes
    suspend fun uploadCallDetails(data: GetCallsRes.GetCallsData): UploadCallDataRes
    suspend fun sendSms(): SendSmsRes
    suspend fun changeStatus(id:RequestBody,status:RequestBody): SendSmsRes
}
