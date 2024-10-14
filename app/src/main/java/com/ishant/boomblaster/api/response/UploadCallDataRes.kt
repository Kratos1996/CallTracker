package com.ishant.boomblaster.api.response

import com.google.gson.annotations.SerializedName


data class UploadCallDataRes(
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("message")
    var message: String?
) {
    data class Data(
        @SerializedName("created_at")
        var createdAt: String?,
        @SerializedName("id")
        var id: Int?,
        @SerializedName("mobile")
        var mobile: String?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("remark")
        var remark: Any?,
        @SerializedName("type")
        var type: Int?,
        @SerializedName("updated_at")
        var updatedAt: Any?,
        @SerializedName("user_id")
        var userId: Int?
    )
}