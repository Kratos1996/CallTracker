package com.ishant.boomblaster.data

import com.ishant.boomblaster.api.request.UploadContactRequest
import com.ishant.boomblaster.api.response.UploadCallDataRes
import com.ishant.boomblaster.api.response.ContactSavedResponse
import com.ishant.boomblaster.api.response.LoginResponse
import com.ishant.boomblaster.api.response.UploadContactResponse
import com.ishant.boomblaster.api.response.UrlResponse
import com.ishant.boomblaster.api.response.getcalls.GetCallsRes
import com.ishant.boomblaster.api.response.sms.SendSmsRes
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
