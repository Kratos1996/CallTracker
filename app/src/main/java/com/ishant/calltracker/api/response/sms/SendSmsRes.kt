package com.ishant.calltracker.api.response.sms


import com.google.gson.annotations.SerializedName

data class SendSmsRes(
    @SerializedName("data")
    val sendSmsData: ArrayList<SendSmsData> = arrayListOf(),
    @SerializedName("smsdata")
    val smsdata: String? = ""

) {
    data class SendSmsData(
        @SerializedName("country_code")
        val countryCode: String?,
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("id")
        val id: Int?,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("message")
        val message: String?,
        @SerializedName("mobile")
        val mobile: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("updated_at")
        val updatedAt: String?,
        @SerializedName("user_id")
        val userId: Int?,
        @SerializedName("status")
        val status: Int?
    )
}