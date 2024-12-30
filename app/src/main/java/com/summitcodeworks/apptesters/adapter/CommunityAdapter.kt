package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.summitcodeworks.apptesters.databinding.ItemListChatLayoutBinding
import com.summitcodeworks.apptesters.models.AppCommunity

class CommunityAdapter(
    private val mContext: Context,
    private var communityList: MutableList<AppCommunity>
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>(

) {

    class ViewHolder(val binding: ItemListChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Add your view bindings here
    }

    fun submitList(newMessages: List<AppCommunity>) {
        communityList = newMessages.toMutableList()
        notifyDataSetChanged() // For simple implementation. Consider using DiffUtil for better performance
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemListChatLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = communityList[position]

        holder.binding.apply {
            textViewSender.text = chat.senderKey
            textViewMessage.text = chat.chatMessage
            textViewTimestamp.text = chat.chatTimestamp

            // If there's an attachment, show the attachment icon
            imageViewAttachment.isVisible = !chat.chatAttachment.isNullOrEmpty()
        }
    }


}