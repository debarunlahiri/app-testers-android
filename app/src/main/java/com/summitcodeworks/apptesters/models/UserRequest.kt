package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("userKey") val userKey: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("userEmail") val userEmail: String
)
