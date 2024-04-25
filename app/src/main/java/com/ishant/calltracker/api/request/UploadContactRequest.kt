package com.ishant.calltracker.api.request

import com.google.gson.annotations.SerializedName
import com.ishant.calltracker.api.response.LoginResponse

data class UploadContactRequest(
    @SerializedName("countryCode")
    var countryCode: LoginResponse.CountryCode? =null,
    @SerializedName("data")
    val data: ArrayList<UploadContactData> = arrayListOf()
) {
    data class UploadContactData(
        val sourceMobileNo: String,
        val mobile: String,
        val type: String,
        val duration: String,
        val name: String,
        val dateTime: String
    )
}
