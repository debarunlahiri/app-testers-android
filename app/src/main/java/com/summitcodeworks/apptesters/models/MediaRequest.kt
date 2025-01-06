package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class MediaRequest(
    @SerializedName("chatId") private var _chatId: Long = -1,
    @SerializedName("mediaUrl") private var _mediaUrl: MutableList<String> = mutableListOf(),
    @SerializedName("mediaType") private var _mediaType: String = "",
    @SerializedName("senderId") private var _senderId: Int = -1,
    @SerializedName("senderKey") private var _senderKey: String = "",
    @SerializedName("chatMessage") private var _chatMessage: String = ""
) {
    var chatId: Long
        get() = _chatId
        set(value) {
            _chatId = value
        }

    var mediaUrl: MutableList<String>
        get() = _mediaUrl
        set(value) {
            _mediaUrl = value
        }

    var mediaType: String
        get() = _mediaType
        set(value) {
            _mediaType = value
        }

    var senderId: Int
        get() = _senderId
        set(value) {
            _senderId = value
        }

    var senderKey: String
        get() = _senderKey
        set(value) {
            _senderKey = value
        }

    var chatMessage: String
        get() = _chatMessage
        set(value) {
            _chatMessage = value
        }

}
