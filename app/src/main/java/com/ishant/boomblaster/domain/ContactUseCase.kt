package com.ishant.boomblaster.domain

import com.ishant.boomblaster.api.request.UploadContactRequest
import com.ishant.boomblaster.api.response.UploadCallDataRes
import com.ishant.boomblaster.api.response.ContactSavedResponse
import com.ishant.boomblaster.api.response.LoginResponse
import com.ishant.boomblaster.api.response.UploadContactResponse
import com.ishant.boomblaster.api.response.UrlResponse
import com.ishant.boomblaster.api.response.getcalls.GetCallsRes
import com.ishant.boomblaster.api.response.sms.SendSmsRes
import com.ishant.boomblaster.network.Resource
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