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
            userName = json["userName"] as? String,
            userEmail = json["userEmail"] as? String,
            chatMessage = json["chatMessage"] as String,
            chatAttachment = json["chatAttachment"] as? String,
            chatTimestamp = json["chatTimestamp"] as? String,
            mediaList = (json["mediaList"] as? List<Map<String, Any?>>)?.map { mediaJson ->
                Media(
                    mediaId = (mediaJson["mediaId"] as? Number)?.toLong(),
                    chatId = (mediaJson["chatId"] as? Number)?.toLong(),
                    mediaUrl = mediaJson["mediaUrl"] as? String,
                    mediaType = mediaJson["mediaType"] as? String,
                    uploadedAt = mediaJson["uploadedAt"] as? String
                )
            } ?: emptyList(),
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
            "mediaList" to appCommunity.mediaList.map { media ->
                mapOf(
                    "mediaId" to media.mediaId,
                    "chatId" to media.chatId,
                    "mediaUrl" to media.mediaUrl,
                    "mediaType" to media.mediaType,
                    "uploadedAt" to media.uploadedAt
                )
            },
            "useFlag" to appCommunity.useFlag
        )
    }
}
