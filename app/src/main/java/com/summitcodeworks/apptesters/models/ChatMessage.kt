package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ChatMessage(
    @SerializedName("chatId") val chatId: Long?,
    @SerializedName("chatSenderId") val chatSenderId: Int,
    @SerializedName("chatReceiverId") val chatReceiverId: Int,
    @SerializedName("chatMessage") val chatMessage: String,
    @SerializedName("chatMedia") val chatMedia: String,
    @SerializedName("chatTimestamp") val chatTimestamp: String?,
    @SerializedName("useFlag") val useFlag: Boolean?
)