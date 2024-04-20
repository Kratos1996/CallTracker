package com.ishant.calltracker.api

import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @FormUrlEncoded
    @POST(ApiConstant.LOGIN)
    suspend fun loginNow(
        @Field("mobile") mobile:String,
        @Field("password") password:String
    ): LoginResponse

    @GET(ApiConstant.GET_CALLS)
    suspend fun getCallDetails(): GetCallsRes

    @FormUrlEncoded
    @POST(ApiConstant.UPLOAD_CONTACT)
    suspend fun uploadContact(
        @Field("source_mobile") sourceMobileNo:String,
        @Field("mobile") mobile:String,
        @Field("type") type:String,
        @Field("duration") duration:String,
        @Field("name") name:String
    ): ContactSavedResponse


    @POST(ApiConstant.UPLOAD_CONTACTS)
    suspend fun uploadContacts(@Body request : UploadContactRequest): UploadContactResponse

    @GET(ApiConstant.CUSTOM_BASE_URL)
    suspend fun getDomains(): UrlResponse
}