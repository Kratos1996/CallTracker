package com.ishant.calltracker.domain

import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.UploadCallDataRes
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.api.response.sms.SendSmsRes
import com.ishant.calltracker.network.Resource
import kotlinx.coroutines.flow.Flow

interface ContactUseCase {
    fun loginNow(mobile:String, password:String ): Flow<Resource<LoginResponse>>

    fun uploadContacts(request : UploadContactRequest): Flow<Resource<UploadContactResponse>>
    fun getDomains():  Flow<Resource<UrlResponse>>
    fun getCallDetails(callType:Int):  Flow<Resource<GetCallsRes>>
    fun uploadCallDetails(data: GetCallsRes.GetCallsData): Flow<Resource<UploadCallDataRes>>
    fun uploadContact(uploadContactData: UploadContactRequest):Flow<Resource<ContactSavedResponse>>
    fun sendSms(): Flow<Resource<SendSmsRes>>
}