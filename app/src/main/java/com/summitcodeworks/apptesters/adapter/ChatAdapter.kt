package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ItemListChatLayoutBinding
import com.summitcodeworks.apptesters.models.ChatMessage
import com.summitcodeworks.apptesters.models.userApps.UserAppsResponse
import com.summitcodeworks.apptesters.utils.SharedPrefsManager

class ChatAdapter(var mContext: Context, var chatList: List<ChatMessage>, var onChatAdapterListener: OnChatAdapterListener) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


    @JvmName("setChatList1")
    fun setChatList(chatList: List<ChatMessage>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemListChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage, onChatAdapterListener: OnChatAdapterListener) {
            // Bind your data to the views using binding
//            binding.tvChatMessage.text = chatMessage.chatMessage
//            binding.tvChatTime.text = chatMessage.chatTimestamp
//
//            if (chatMessage.chatSenderId == SharedPrefsManager.getUserDetails(binding.root.context).userId) {
//                binding.llChatMessage.gravity = GravityCompat.END
//            } else {
//                binding.llChatMessage.gravity = GravityCompat.START
//            }
//            binding.root.setOnClickListener {
//                onChatAdapterListener.onChatClick(chatMessage)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemListChatLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = chatList[position]

    }


    interface OnChatAdapterListener {
        fun onChatClick(chatMessage: ChatMessage)
    }
}