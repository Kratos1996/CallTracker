package com.ishant.jagtap.api.response


import com.google.gson.annotations.SerializedName

data class UrlResponse(
    @SerializedName("data")
    var urlResponseData: List<Data> = arrayListOf(),
    @SerializedName("status")
    var status: Int?=  null
) {
    data class Data(
        @SerializedName("created_at")
        var createdAt: String? =  null,
        @SerializedName("id")
        var id: Int? =  null ,
        @SerializedName("status")
        var status: Int? =  null,
        @SerializedName("updated_at")
        var updatedAt: Any? =  null,
        @SerializedName("url_name")
        var urlName: String? =  null ,
        @SerializedName("url_value")
        var urlValue: String?= null
    )
}