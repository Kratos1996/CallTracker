package com.ishant.jagtap.data

import com.ishant.jagtap.api.ApiInterface
import com.ishant.jagtap.api.request.UploadContactRequest
import com.ishant.jagtap.api.response.LoginResponse
import com.ishant.jagtap.api.response.UploadContactResponse
import com.ishant.jagtap.api.response.UrlResponse
import com.ishant.jagtap.api.response.getcalls.GetCallsRes
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl  @Inject constructor(val api: ApiInterface) :ContactRepository {

    override suspend fun loginNow(mobile: String, password: String): LoginResponse = api.loginNow(mobile, password)
    override suspend fun uploadContact(uploadContactData: UploadContactRequest ) = api.uploadContact(uploadContactData)

    override suspend fun uploadContacts(request: UploadContactRequest): UploadContactResponse = api.uploadContacts(request)
    override suspend fun getDomains(): UrlResponse = api.getDomains()
    override suspend fun getCallDetails(callType:Int): GetCallsRes = api.getCallDetails(callType)
    override suspend fun uploadCallDetails(data: GetCallsRes.GetCallsData) = api.uploadCallDetails(data)
    override  suspend fun sendSms() = api.sendSms()
    override  suspend fun changeStatus(id:RequestBody,status:RequestBody) = api.changeStatus(id, status)

}