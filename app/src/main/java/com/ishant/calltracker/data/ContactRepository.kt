package com.ishant.calltracker.data

import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse


interface ContactRepository {

    suspend fun loginNow(mobile:String, password:String ): LoginResponse

    suspend fun uploadContact(sourceMobileNo:String, mobile:String, name:String,type:String  ): ContactSavedResponse
}