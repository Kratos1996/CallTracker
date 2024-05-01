package com.ishant.calltracker.api.response.getcalls


import com.google.gson.annotations.SerializedName

data class GetCallsRes(
    @SerializedName("data")
    var getCallsData: ArrayList<GetCallsData> = arrayListOf()
) {
    data class GetCallsData(
        @SerializedName("created_at")
        var createdAt: String?,
        @SerializedName("id")
        var id: Int?,
        @SerializedName("mobile")
        var mobile: String?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("remark")
        var remark: String?,
        @SerializedName("type")
        var type: Int?,
        @SerializedName("updated_at")
        var updatedAt: Any?,
        @SerializedName("user_id")
        var userId: Int?
    )
}