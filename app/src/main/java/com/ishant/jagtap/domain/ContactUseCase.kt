package com.ishant.jagtap.domain

import com.ishant.jagtap.api.request.UploadContactRequest
import com.ishant.jagtap.api.response.UploadCallDataRes
import com.ishant.jagtap.api.response.ContactSavedResponse
import com.ishant.jagtap.api.response.LoginResponse
import com.ishant.jagtap.api.response.UploadContactResponse
import com.ishant.jagtap.api.response.UrlResponse
import com.ishant.jagtap.api.response.getcalls.GetCallsRes
import com.ishant.jagtap.api.response.sms.SendSmsRes
import com.ishant.jagtap.network.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody

interface ContactUseCase {
    fun loginNow(mobile:String, password:String ): Flow<Resource<LoginResponse>>

    fun uploadContacts(request : UploadContactRequest): Flow<Resource<UploadContactResponse>>
    fun getDomains():  Flow<Resource<UrlResponse>>
    fun getCallDetails(callType:Int):  Flow<Resource<GetCallsRes>>
    fun uploadCallDetails(data: GetCallsRes.GetCallsData): Flow<Resource<UploadCallDataRes>>
    fun uploadContact(uploadContactData: UploadContactRequest):Flow<Resource<ContactSavedResponse>>
    fun sendSms(): Flow<Resource<SendSmsRes>>
    fun changeStatus(id: RequestBody, status: RequestBody): Flow<Resource<SendSmsRes>>
}