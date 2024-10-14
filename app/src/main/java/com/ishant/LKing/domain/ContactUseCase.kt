package com.ishant.LKing.domain

import com.ishant.LKing.api.request.UploadContactRequest
import com.ishant.LKing.api.response.UploadCallDataRes
import com.ishant.LKing.api.response.ContactSavedResponse
import com.ishant.LKing.api.response.LoginResponse
import com.ishant.LKing.api.response.UploadContactResponse
import com.ishant.LKing.api.response.UrlResponse
import com.ishant.LKing.api.response.getcalls.GetCallsRes
import com.ishant.LKing.api.response.sms.SendSmsRes
import com.ishant.LKing.network.Resource
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