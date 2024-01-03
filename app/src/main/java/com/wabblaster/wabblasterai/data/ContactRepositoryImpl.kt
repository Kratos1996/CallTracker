package com.wabblaster.wabblasterai.data

import com.wabblaster.wabblasterai.api.ApiInterface
import com.wabblaster.wabblasterai.api.response.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl  @Inject constructor(val api: ApiInterface) :ContactRepository {

    override suspend fun loginNow(mobile: String, password: String): LoginResponse = api.loginNow(mobile, password)

    override suspend fun uploadContact(sourceMobileNo: String, mobile: String, name: String,type:String ) =
        api.uploadContact(
            sourceMobileNo = sourceMobileNo,
            mobile = mobile,
            name = name,
            type = type
        )

}