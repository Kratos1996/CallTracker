package com.ishant.calltracker.api

import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.utils.AppPreference
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {


    @FormUrlEncoded
    @POST("login")
    suspend fun loginNow(@Field("mobile") mobile:String,@Field("password") password:String ): LoginResponse

    @FormUrlEncoded
    @POST("contact")
    suspend fun uploadContact(
        @Field("source_mobile") sourceMobileNo:String,
        @Field("mobile") mobile:String,
        @Field("type") type:String,
        @Field("duration") duration:String,
        @Field("name") name:String ): ContactSavedResponse


    @POST("v2/contact")
    suspend fun uploadContacts(@Body request : UploadContactRequest): UploadContactResponse

    @GET("https://wappblaster.in/api/domain")
    suspend fun getDomains(): UrlResponse
}