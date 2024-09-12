package com.ishant.calltracker.api

import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.UploadCallDataRes
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.api.response.sms.SendSmsRes
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiInterface {

    @FormUrlEncoded
    @POST(ApiConstant.LOGIN)
    suspend fun loginNow(
        @Field("mobile") mobile:String,
        @Field("password") password:String
    ): LoginResponse

    @GET(ApiConstant.GET_CALLS+"/{callType}")
    suspend fun getCallDetails(@Path("callType") callType:Int): GetCallsRes

    @POST(ApiConstant.GET_CALLS)
    suspend fun uploadCallDetails(@Body data: GetCallsRes.GetCallsData): UploadCallDataRes


    @POST(ApiConstant.UPLOAD_WHATSAPP_CONTACT)
    suspend fun uploadContact(@Body data: UploadContactRequest): ContactSavedResponse


    @POST(ApiConstant.UPLOAD_CONTACTS)
   suspend fun uploadContacts(@Body request : UploadContactRequest): UploadContactResponse

    @GET(ApiConstant.CUSTOM_BASE_URL)
    suspend fun getDomains(): UrlResponse

    @GET(ApiConstant.SEND_SMS)
    suspend fun sendSms(): SendSmsRes

    @Multipart
    @POST(ApiConstant.CHANGE_STATUS)
    suspend fun changeStatus(@Part("id")id:RequestBody,@Part("status")status:RequestBody,): SendSmsRes
}