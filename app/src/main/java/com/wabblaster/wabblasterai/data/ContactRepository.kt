package com.wabblaster.wabblasterai.data

import com.wabblaster.wabblasterai.api.response.ContactSavedResponse
import com.wabblaster.wabblasterai.api.response.LoginResponse


interface ContactRepository {

    suspend fun loginNow(mobile:String, password:String ): LoginResponse

    suspend fun uploadContact(sourceMobileNo:String, mobile:String, name:String,type:String  ): ContactSavedResponse
}