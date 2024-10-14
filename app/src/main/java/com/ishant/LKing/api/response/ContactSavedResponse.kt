package com.ishant.LKing.api.response


import com.google.gson.annotations.SerializedName

data class ContactSavedResponse(
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("status")
    var status: Int?
) {
    data class Data(
        @SerializedName("country_code")
        var countryCode: String?,
        @SerializedName("created_at")
        var createdAt: String?,
        @SerializedName("id")
        var id: Int?,
        @SerializedName("mobile")
        var mobile: String?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("source_mobile")
        var sourceMobile: String?,
        @SerializedName("status")
        var status: Int?,
        @SerializedName("updated_at")
        var updatedAt: String?,
        @SerializedName("user_id")
        var userId: Int?
    )
}

data class UploadContactResponse(var status: Int?=null)
