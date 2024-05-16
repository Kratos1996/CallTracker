package com.ishant.calltracker.data

import com.ishant.calltracker.api.ApiInterface
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl  @Inject constructor(val api: ApiInterface) :ContactRepository {

    override suspend fun loginNow(mobile: String, password: String): LoginResponse = api.loginNow(mobile, password)
    override suspend fun uploadContact(uploadContactData: UploadContactRequest.UploadContactData ) = api.uploadContact(uploadContactData)

    override suspend fun uploadContacts(request: UploadContactRequest): UploadContactResponse = api.uploadContacts(request)
    override suspend fun getDomains(): UrlResponse = api.getDomains()
    override suspend fun getCallDetails(callType:Int): GetCallsRes = api.getCallDetails(callType)
    override suspend fun uploadCallDetails(data: GetCallsRes.GetCallsData) = api.uploadCallDetails(data)

}