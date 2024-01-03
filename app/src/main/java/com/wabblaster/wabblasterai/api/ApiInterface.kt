package com.wabblaster.wabblasterai.api

import com.wabblaster.wabblasterai.api.response.ContactSavedResponse
import com.wabblaster.wabblasterai.api.response.LoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
        @Field("name") name:String ): ContactSavedResponse

}