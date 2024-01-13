package com.ishant.calltracker.domain

import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.network.Resource
import kotlinx.coroutines.flow.Flow

interface ContactUseCase {
    fun loginNow(mobile:String, password:String ): Flow<Resource<LoginResponse>>

     fun uploadContact(sourceMobileNo:String, mobile:String, name:String ,type:String ):Flow<Resource<ContactSavedResponse>>
}