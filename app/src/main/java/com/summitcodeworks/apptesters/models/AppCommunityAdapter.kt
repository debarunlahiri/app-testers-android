package com.summitcodeworks.apptesters.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.io.Serializable

class AppCommunityAdapter {
    @FromJson
    fun fromJson(json: Map<String, Any?>): AppCommunity {
        return AppCommunity(
            chatId = (json["chatId"] as? Number)?.toLong(),
            senderId = (json["senderId"] as Number).toInt(),
            senderKey = json["senderKey"] as String,
            userName = json["userName"] as String,
            userEmail = json["userEmail"] as String,
            chatMessage = json["chatMessage"] as String,
            chatAttachment = json["chatAttachment"] as? String,
            chatTimestamp = json["chatTimestamp"] as? String,
            useFlag = json["useFlag"] as? Boolean ?: true
        )
    }

    @ToJson
    fun toJson(appCommunity: AppCommunity): Map<String, Any?> {
        return mapOf(
            "chatId" to appCommunity.chatId,
            "senderId" to appCommunity.senderId,
            "senderKey" to appCommunity.senderKey,
            "userName" to appCommunity.userName,
            "userEmail" to appCommunity.userEmail,
            "chatMessage" to appCommunity.chatMessage,
            "chatAttachment" to appCommunity.chatAttachment,
            "chatTimestamp" to appCommunity.chatTimestamp,
            "useFlag" to appCommunity.useFlag
        )
    }
}