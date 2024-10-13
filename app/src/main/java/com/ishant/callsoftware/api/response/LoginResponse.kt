package com.ishant.callsoftware.api.response


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("countryCode")
    var countryCode: CountryCode?= null,
    @SerializedName("token")
    var token: String? = null,
    @SerializedName("user")
    var user: User? = null
) {
    data class CountryCode(
        @SerializedName("country_code")
        var countryCode: String?,
        @SerializedName("id")
        var id: Int?,
        @SerializedName("replace_from")
        var replaceFrom: String?
    )
    data class User(
        @SerializedName("city")
        var city: Any? = null ,
        @SerializedName("company_name")
        var companyName: String? = null ,
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("email")
        var email: String? = null,
        @SerializedName("email_verified_at")
        var emailVerifiedAt: Any? = null,
        @SerializedName("id")
        var id: Int? = null,
        @SerializedName("max_allowed_member")
        var maxAllowedMember: Int? = null,
        @SerializedName("mobile")
        var mobile: String? = null,
        @SerializedName("name")
        var name: String ? = null,
        @SerializedName("referal_code")
        var referalCode: Any? = null,
        @SerializedName("slug")
        var slug: String? = null,
        @SerializedName("state")
        var state: Any? = null,
        @SerializedName("status")
        var status: Int? = null,
        @SerializedName("updated_at")
        var updatedAt: String? = null
    )
}