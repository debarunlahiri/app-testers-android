package com.summitcodeworks.apptesters.utils

import androidx.recyclerview.widget.DiffUtil
import com.summitcodeworks.apptesters.models.AppCommunity

class CommunityDiffCallback : DiffUtil.ItemCallback<AppCommunity>() {
    override fun areItemsTheSame(oldItem: AppCommunity, newItem: AppCommunity): Boolean {
        return oldItem.chatId == newItem.chatId
    }

    override fun areContentsTheSame(oldItem: AppCommunity, newItem: AppCommunity): Boolean {
        return oldItem == newItem
    }
}