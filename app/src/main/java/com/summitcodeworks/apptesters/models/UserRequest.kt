package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("user_key") val userKey: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_email") val userEmail: String
)
