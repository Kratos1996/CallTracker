package com.wabblaster.wabblasterai.domain

import com.google.gson.Gson
import com.wabblaster.wabblasterai.api.response.ContactSavedResponse
import com.wabblaster.wabblasterai.api.response.LoginResponse
import com.wabblaster.wabblasterai.data.ContactRepository
import com.wabblaster.wabblasterai.network.Resource
import com.wabblaster.wabblasterai.network.catchExceptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactUseCaseImpl @Inject constructor(private val repository: ContactRepository):ContactUseCase {
    override fun loginNow(mobile: String, password: String): Flow<Resource<LoginResponse>> = flow {
        try {
            emit(Resource.Loading<LoginResponse>())
            val response= repository.loginNow(mobile,password)

            emit(Resource.Success<LoginResponse>(response))

        } catch (e: Exception) {
            catchExceptions<LoginResponse>(e,Gson())
        }
    }

    override fun uploadContact(sourceMobileNo: String, mobile: String, name: String,type:String ):Flow<Resource<ContactSavedResponse>> = flow {
        try {
            emit(Resource.Loading<ContactSavedResponse>())
            val response= repository.uploadContact(
                sourceMobileNo = sourceMobileNo,
                mobile = mobile,
                name = name,
                type = type)

            emit(Resource.Success<ContactSavedResponse>(response))

        } catch (e: Exception) {
            catchExceptions<ContactSavedResponse>(e,Gson())
        }
    }
}