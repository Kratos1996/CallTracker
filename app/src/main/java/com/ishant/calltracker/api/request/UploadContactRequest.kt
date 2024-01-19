package com.ishant.calltracker.api.request

import retrofit2.http.Field

data class UploadContactRequest(val data: ArrayList<UploadContactData> = arrayListOf()) {
    data class UploadContactData(
        val sourceMobileNo: String,
        val mobile: String,
        val type: String,
        val duration: String,
        val name: String,
        val dateTime: String
    )
}
