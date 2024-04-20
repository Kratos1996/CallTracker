package com.ishant.calltracker.data

import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes


interface ContactRepository {

    suspend fun loginNow(mobile:String, password:String ): LoginResponse

    suspend fun uploadContact(sourceMobileNo:String, mobile:String, name:String,type:String,duration:String  ): ContactSavedResponse

    suspend fun uploadContacts(request : UploadContactRequest): UploadContactResponse

    suspend fun getDomains(): UrlResponse
    suspend fun getCallDetails(): GetCallsRes
}
