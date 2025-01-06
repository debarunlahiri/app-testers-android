package com.summitcodeworks.apptesters.models

import java.io.Serializable

data class AppCommunity(
    val chatId: Long? = null,
    val senderId: Int,
    val senderKey: String,
    val userName: String? = null,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null,
    val chatMessage: String,
    val chatAttachment: String? = null,
    val chatTimestamp: String? = null,
    val mediaList: List<Media> = emptyList(),
    val useFlag: Boolean = true,
): Serializable {
    override fun equals(other: Any?): Boolean {
        return other is AppCommunity && this.chatId == other.chatId
    }

    override fun hashCode(): Int {
        return chatId.hashCode()
    }
}

data class Media(
    val mediaId: Long? = null,
    val chatId: Long? = -1,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val uploadedAt: String? = null
): Serializable