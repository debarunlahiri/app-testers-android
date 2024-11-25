package com.summitcodeworks.apptesters.apiClient

import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keeps the connection alive indefinitely
        .build()

    private var listener: WebSocketListener? = null

    fun connectWebSocket(serverUrl: String, messageCallback: (String) -> Unit) {
        val request = Request.Builder().url(serverUrl).build()
        listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Received message: $text")
                messageCallback(text) // Pass the received message to the callback
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Received binary message")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket connection failed: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closed: $reason")
            }
        }
        webSocket = client.newWebSocket(request, listener!!)
    }

    fun sendMessage(message: String) {
        if (::webSocket.isInitialized) {
            webSocket.send(message)
            println("Message sent: $message")
        } else {
            println("WebSocket is not connected")
        }
    }

    fun disconnectWebSocket() {
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Closing connection")
        }
    }
}
