package com.ishant.LKing.domain

import com.google.gson.Gson
import com.ishant.LKing.api.request.UploadContactRequest
import com.ishant.LKing.api.response.UploadCallDataRes
import com.ishant.LKing.api.response.ContactSavedResponse
import com.ishant.LKing.api.response.LoginResponse
import com.ishant.LKing.api.response.UploadContactResponse
import com.ishant.LKing.api.response.UrlResponse
import com.ishant.LKing.api.response.getcalls.GetCallsRes
import com.ishant.LKing.api.response.sms.SendSmsRes
import com.ishant.LKing.data.ContactRepository
import com.ishant.LKing.network.Resource
import com.ishant.LKing.network.catchExceptions
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactUseCaseImpl @Inject constructor(private val repository: ContactRepository):ContactUseCase {
    override fun loginNow(mobile: String, password: String) = flow {
        try {
            emit(Resource.Loading<LoginResponse>())
            val response= repository.loginNow(mobile,password)
            emit(Resource.Success<LoginResponse>(response))
        } catch (e: Exception) { catchExceptions<LoginResponse>(e,Gson())
        }
    }

    override fun uploadContacts(request: UploadContactRequest) = flow {
        //Log.e("uploadContacts","BaseUrl : ${AppPreference.baseUrl}")

        try {
            emit(Resource.Loading<UploadContactResponse>())
            val response= repository.uploadContacts(request)
            emit(Resource.Success<UploadContactResponse>(response))
        } catch (e: Exception) {
            catchExceptions<UploadContactResponse>(e,Gson())
        }
    }

    override fun getDomains()  = flow {
        try {
            emit(Resource.Loading<UrlResponse>())
            val response= repository.getDomains()
            emit(Resource.Success<UrlResponse>(response))
        } catch (e: Exception) {
            catchExceptions<UrlResponse>(e,Gson())
        }
    }

    override fun getCallDetails(callType:Int) = flow {
        try {
            emit(Resource.Loading<GetCallsRes>())
            val response= repository.getCallDetails(callType)
            emit(Resource.Success<GetCallsRes>(response))
        } catch (e: Exception) {
            catchExceptions<GetCallsRes>(e,Gson())
        }
    }

    override fun uploadCallDetails(data: GetCallsRes.GetCallsData) = flow {

        try {
            emit(Resource.Loading<UploadCallDataRes>())
            val response= repository.uploadCallDetails(data)
            emit(Resource.Success<UploadCallDataRes>(response))
        } catch (e: Exception) {
            catchExceptions<UploadCallDataRes>(e,Gson())
        }
    }

    override fun uploadContact(uploadContactData: UploadContactRequest ) = flow {

        try {
            emit(Resource.Loading<ContactSavedResponse>())
            val response= repository.uploadContact(uploadContactData)
            emit(Resource.Success<ContactSavedResponse>(response))
        } catch (e: Exception) {
            catchExceptions<ContactSavedResponse>(e,Gson())
        }
    }

    override fun sendSms() = flow {
        try {
            emit(Resource.Loading<SendSmsRes>())
            val response= repository.sendSms()
            emit(Resource.Success<SendSmsRes>(response))
        } catch (e: Exception) {
            catchExceptions<SendSmsRes>(e,Gson())
        }
    }
    override fun changeStatus(id:RequestBody,status:RequestBody) = flow {
        try {
            emit(Resource.Loading<SendSmsRes>())
            val response= repository.changeStatus(id,status)
            emit(Resource.Success<SendSmsRes>(response))
        } catch (e: Exception) {
            catchExceptions<SendSmsRes>(e,Gson())
        }
    }
}