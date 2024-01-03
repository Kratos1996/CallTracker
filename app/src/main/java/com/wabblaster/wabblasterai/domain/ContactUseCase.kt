package com.wabblaster.wabblasterai.domain

import com.wabblaster.wabblasterai.api.response.ContactSavedResponse
import com.wabblaster.wabblasterai.api.response.LoginResponse
import com.wabblaster.wabblasterai.network.Resource
import kotlinx.coroutines.flow.Flow

interface ContactUseCase {
    fun loginNow(mobile:String, password:String ): Flow<Resource<LoginResponse>>

     fun uploadContact(sourceMobileNo:String, mobile:String, name:String ,type:String ):Flow<Resource<ContactSavedResponse>>
}