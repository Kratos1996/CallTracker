package com.ishant.calltracker.domain

import com.google.gson.Gson
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.data.ContactRepository
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.network.catchExceptions
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

    override fun uploadContacts(request: UploadContactRequest): Flow<Resource<UploadContactResponse>> = flow {
        try {
            emit(Resource.Loading<UploadContactResponse>())
            val response= repository.uploadContacts(request)

            emit(Resource.Success<UploadContactResponse>(response))

        } catch (e: Exception) {
            catchExceptions<UploadContactResponse>(e,Gson())
        }
    }

    override fun uploadContact(sourceMobileNo: String, mobile: String, name: String,type:String,duration:String ):Flow<Resource<ContactSavedResponse>> = flow {
        try {
            emit(Resource.Loading<ContactSavedResponse>())
            val response= repository.uploadContact(
                sourceMobileNo = sourceMobileNo,
                mobile = mobile,
                name = name,
                type = type,
                duration =duration
            )

            emit(Resource.Success<ContactSavedResponse>(response))

        } catch (e: Exception) {
            catchExceptions<ContactSavedResponse>(e,Gson())
        }
    }
}