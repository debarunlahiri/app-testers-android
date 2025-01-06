package com.summitcodeworks.apptesters.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.apptesters.apiClient.WebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val webSocketService = WebSocketService("ws://192.168.0.162:8082/ws/websocket")

    private val _messages = MutableStateFlow<List<AppCommunity>>(emptyList())
    val messages = _messages.asStateFlow()

    val connectionStatus = webSocketService.connectionStatus

    init {
        viewModelScope.launch {
            webSocketService.messageFlow.collect { message ->
                Log.d("ChatViewModel", "Received message in ViewModel: $message")
                _messages.update { currentList ->
                    (currentList + message).sortedByDescending { it.chatTimestamp }
                }
            }
        }

        viewModelScope.launch {
            connectionStatus.collect { status ->
                Log.d("ChatViewModel", "Connection status: $status")
                if (status == WebSocketService.ConnectionStatus.Disconnected) {
                    delay(5000) // Wait 5 seconds before reconnecting
                    webSocketService.connect()
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                webSocketService.connect()
                webSocketService.fetchMessages()
            } catch (e: Exception) {
                Log.e("WebSocket", "Error during WebSocket operations", e)
            }
        }
    }


    fun sendMessage(message: String, senderId: Int, senderKey: String) {
        val chat = AppCommunity(
            chatId = -1,
            senderId = senderId,
            senderKey = senderKey,
            chatMessage = message
        )
        webSocketService.sendMessage(chat)
    }

    fun sendMessageWithMedia(message: String, senderId: Int, senderKey: String, mediaList: List<Media>) {
        val chat = AppCommunity(
            chatId = -1,
            senderId = senderId,
            senderKey = senderKey,
            chatMessage = message,
            mediaList = mediaList
        )
        webSocketService.sendMessage(chat)
    }

    fun convertCommunityMediaToMediaList(
        communityMediaList: List<CommunityMedia>,
    ): List<Media> {
        return communityMediaList.map { communityMedia ->
            Media(
                chatId = -1,
                mediaUrl = communityMedia.mediaUri.toString(), // Convert Uri to String
                mediaType = "image", // Optional parameter, pass if available
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}