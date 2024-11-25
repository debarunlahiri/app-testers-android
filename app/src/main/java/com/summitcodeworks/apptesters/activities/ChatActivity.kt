package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.events.EventHandler
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.adapter.ChatAdapter
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.apiClient.WebSocketManager
import com.summitcodeworks.apptesters.databinding.ActivityChatBinding
import com.summitcodeworks.apptesters.models.ChatMessage
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity(), ChatAdapter.OnChatAdapterListener {

    private val TAG: String? = ChatActivity::class.simpleName
    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityChatBinding

    private lateinit var chatAdapter: ChatAdapter
    private var chatList: List<ChatMessage> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val webSocketManager = WebSocketManager()
    val serverUrl = "ws://apptesters-backend.onrender.com/ws/websocket"
    private var receiverId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this
        receiverId = intent.getIntExtra("receiver_id", 0)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
    }


    private fun setupUI() {
        viewBinding.tbChat.visibility = View.VISIBLE
        viewBinding.tbChat.title = "Chat Support"

        setSupportActionBar(viewBinding.tbChat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbChat.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbChat.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbChat.setTitleTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorWhite
                    )
                )
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbChat.setTitleTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorWhite
                    )
                )
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }

        chatAdapter = ChatAdapter(mContext, chatList, this)
        linearLayoutManager = LinearLayoutManager(mContext)
        viewBinding.rvChat.layoutManager = linearLayoutManager
        viewBinding.rvChat.adapter = chatAdapter
        viewBinding.rvChat.setHasFixedSize(true)


        webSocketManager.connectWebSocket(serverUrl) { receivedMessage ->
            runOnUiThread {
                println("New message: $receivedMessage")
            }
        }

        viewBinding.cvChatSend.setOnClickListener {
            Log.d(TAG, "serverUrl: $serverUrl")
            val message = viewBinding.etChatMessage.text.toString()
            if (message.isEmpty()) {
                CommonUtils.showToastLong(mContext, "Please enter a message")
                return@setOnClickListener
            } else {
                viewBinding.etChatMessage.setText("")
                val messageToSend = """
                    {
                        "chatSenderId": ${SharedPrefsManager.getUserDetails(mContext).userId},
                        "chatReceiverId": $receiverId,
                        "chatMessage": $message,
                        "chatStatus": "active"
                    }
                """
                webSocketManager.sendMessage(messageToSend)
            }
        }
    }


    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbChat.navigationIcon = wrappedIcon
        }
    }

    override fun onChatClick(chatMessage: ChatMessage) {


    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnectWebSocket()
    }
}