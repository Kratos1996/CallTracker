package com.ishant.calltracker.domain

import android.util.Log
import com.google.gson.Gson
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.api.response.UploadCallDataRes
import com.ishant.calltracker.api.response.ContactSavedResponse
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.api.response.UploadContactResponse
import com.ishant.calltracker.api.response.UrlResponse
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.api.response.sms.SendSmsRes
import com.ishant.calltracker.data.ContactRepository
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.network.catchExceptions
import com.ishant.calltracker.utils.AppPreference
import kotlinx.coroutines.flow.Flow
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
        Log.d("TAG", "uploadContact1: "+request)
        try {
            emit(Resource.Loading<UploadContactResponse>())
//            val response= repository.uploadContacts(request)
//            emit(Resource.Success<UploadContactResponse>(response))
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
        Log.d("TAG", "uploadContact2: "+data)
        try {
            emit(Resource.Loading<UploadCallDataRes>())
//            val response= repository.uploadCallDetails(data)
//            emit(Resource.Success<UploadCallDataRes>(response))
        } catch (e: Exception) {
            catchExceptions<UploadCallDataRes>(e,Gson())
        }
    }

    override fun uploadContact(uploadContactData: UploadContactRequest ) = flow {
        Log.d("TAG", "uploadContact3: "+uploadContactData)
        try {
            emit(Resource.Loading<ContactSavedResponse>())
//            val response= repository.uploadContact(uploadContactData)
//            emit(Resource.Success<ContactSavedResponse>(response))
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