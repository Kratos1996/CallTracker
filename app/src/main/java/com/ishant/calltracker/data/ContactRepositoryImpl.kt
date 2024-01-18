package com.ishant.calltracker.data

import com.ishant.calltracker.api.ApiInterface
import com.ishant.calltracker.api.response.LoginResponse
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl  @Inject constructor(val api: ApiInterface) :ContactRepository {

    override suspend fun loginNow(mobile: String, password: String): LoginResponse = api.loginNow(mobile, password)

    override suspend fun uploadContact(sourceMobileNo: String, mobile: String, name: String,type:String,duration: String ) =
        api.uploadContact(
            sourceMobileNo = sourceMobileNo,
            mobile = mobile,
            name = name,
            type = type,
            duration = duration
        )

}