package com.summitcodeworks.apptesters.apiClient

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.summitcodeworks.apptesters.models.AppCommunity
import com.summitcodeworks.apptesters.models.AppCommunityAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketService(private val serverUrl: String) {
    private val TAG: String? = WebSocketService::class.simpleName
    private val _messageFlow = MutableSharedFlow<AppCommunity>()
    val messageFlow: SharedFlow<AppCommunity> = _messageFlow

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val moshi = Moshi.Builder()
        .add(AppCommunityAdapter())
        .build()

    private val singleChatAdapter = moshi.adapter(AppCommunity::class.java)
    private val listType = Types.newParameterizedType(List::class.java, AppCommunity::class.java)
    private val chatListAdapter: JsonAdapter<List<AppCommunity>> = moshi.adapter(listType)

    private var webSocket: WebSocketClient? = null

    enum class ConnectionStatus {
        Connected, Disconnected, Error
    }

    suspend fun connect() {
        if (webSocket?.isOpen == true) return
        val connectJob = kotlinx.coroutines.CompletableDeferred<Unit>()
        webSocket = object : WebSocketClient(URI(serverUrl)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connection opened")
                _connectionStatus.tryEmit(ConnectionStatus.Connected)
                connectJob.complete(Unit)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        sendConnectFrame()
                        sendSubscribeFrame()
                        sendHistorySubscribeFrame()
                    } catch (e: Exception) {
                        Log.e("WebSocket", "Error in onOpen suspend functions", e)
                    }
                }
            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received message: $message")
                message?.let {
                    try {
                        if (message.startsWith("MESSAGE")) {
                            val destination = Regex("destination:([^\\n]+)").find(message)?.groupValues?.get(1)
                            val payloadStart = message.indexOf("\n\n") + 2
                            val jsonPayload = message.substring(payloadStart).trimEnd('\u0000')

                            when (destination) {
                                "/topic/community" -> handleSingleChatMessage(jsonPayload)
                                "/topic/community-history" -> handleChatHistoryMessage(jsonPayload)
                                else -> Log.d("WebSocket", "Unhandled destination: $destination")
                            }
                        } else {
                            Log.d("WebSocket", "Unhandled message: $message")
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocket", "Error processing message", e)
                    }
                }
            }


            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Connection closed: $reason")
                _connectionStatus.tryEmit(ConnectionStatus.Disconnected)
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error occurred", ex)
                _connectionStatus.tryEmit(ConnectionStatus.Error)
                connectJob.completeExceptionally(ex ?: Exception("Unknown WebSocket error"))
            }
        }
        webSocket?.connect()

        try {
            connectJob.await() // Wait until onOpen is triggered
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to WebSocket", e)
            throw e
        }
    }

    private fun handleSingleChatMessage(jsonPayload: String) {
        try {
            val chat = singleChatAdapter.fromJson(jsonPayload)
            chat?.let {
                Log.d("WebSocket", "Parsed chat: $it")
                viewModelScope.launch { _messageFlow.emit(it) }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing single chat message", e)
        }
    }

    private fun handleChatHistoryMessage(jsonPayload: String) {
        try {
            val history = chatListAdapter.fromJson(jsonPayload)
            history?.let { chatList ->
                Log.d("WebSocket", "Parsed chat history: $chatList")
                viewModelScope.launch {
                    _messageFlow.emitAll(chatList.asFlow())
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing chat history", e)
        }
    }

    suspend fun fetchMessages() {
        try {
            connect() // Ensure connection is established before sending
            if (webSocket?.isOpen == true) {
                val fetchFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/fetch-messages\n")
                    append("\n") // Empty line before payload
                    append("\u0000") // Null terminator
                }
                Log.i(TAG, "fetchFrame: $fetchFrame")
                webSocket?.send(fetchFrame)
            } else {
                Log.e(TAG, "WebSocket is not connected. Cannot fetch messages.")
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error fetching messages", e)
        }
    }

    suspend fun reconnect() {
        Log.i(TAG, "Attempting to reconnect WebSocket...")
        disconnect()
        connect()
        sendConnectFrame()
        sendSubscribeFrame()
        sendHistorySubscribeFrame()
    }

    private suspend fun sendHistorySubscribeFrame(page: Int = 0, size: Int = 10) {
        try {
            connect()
            val subscribeFrame = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-1\n")
                append("destination:/topic/community-history?page=$page&size=$size\n")
                append("\n")
                append("\u0000")
            }
            Log.i(TAG, "subscribeHistoryFrame: $subscribeFrame")
            webSocket?.send(subscribeFrame)
        } catch (e: Exception) {
            Log.e("WebSocket", "Error subscribing to history topic", e)
        }
    }

    suspend fun sendConnectFrame() {
        try {
            connect()
            val connectFrame = buildString {
                append("CONNECT\n")
                append("accept-version:1.1,1.0\n")
                append("heart-beat:10000,10000\n")
                append("\n")
                append("\u0000")
            }
            Log.i(TAG, "sendConnectFrame: $connectFrame")
            webSocket?.send(connectFrame)
        } catch (e: Exception) {
            Log.e("WebSocket", "Error sending CONNECT frame", e)
        }
    }

    private fun sendSubscribeFrame() {
        try {
            val subscribeFrame = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-0\n")
                append("destination:/topic/community\n")
                append("\n") // Empty line before the body (required in STOMP)
                append("\u0000") // Null terminator to indicate end of frame
            }

            Log.i(TAG, "subscribeFrame: $subscribeFrame")
            webSocket?.send(subscribeFrame)
        } catch (e: Exception) {
            Log.e("WebSocket", "Error sending SUBSCRIBE frame", e)
        }
    }

    fun sendMessage(chat: AppCommunity) {
        try {
            val messageJson = singleChatAdapter.toJson(chat)
            Log.d("WebSocket", "Sending message: $messageJson")

            val sendFrame = buildString {
                append("SEND\n")
                append("destination:/app/community-chat\n")
                append("content-type:application/json\n")
                append("content-length:${messageJson.length}\n")
                append("\n") // Empty line before body
                append(messageJson)
                append("\u0000") // Null character at the end of the frame
            }

            Log.i(TAG, "sendFrame: $sendFrame")
            webSocket?.send(sendFrame)
        } catch (e: Exception) {
            Log.e("WebSocket", "Error sending message", e)
        }
    }

    fun disconnect() {
        try {
            val disconnectFrame = """
                DISCONNECT
                receipt:bye

                \u0000
            """.trimIndent()
            Log.i(TAG, "disconnectFrame: $disconnectFrame")
            webSocket?.send(disconnectFrame)
            webSocket?.close()
        } catch (e: Exception) {
            Log.e("WebSocket", "Error disconnecting", e)
        } finally {
            webSocket = null
        }
    }

    companion object {
        private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}