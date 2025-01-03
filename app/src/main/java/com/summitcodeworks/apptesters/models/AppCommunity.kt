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
    val useFlag: Boolean = true
): Serializable