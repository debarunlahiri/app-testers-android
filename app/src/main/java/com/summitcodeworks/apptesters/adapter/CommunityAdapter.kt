package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.transform.CircleCropTransformation
import com.summitcodeworks.apptesters.databinding.ItemListChatLayoutBinding
import com.summitcodeworks.apptesters.models.AppCommunity
import com.summitcodeworks.apptesters.utils.CommonUtils
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class CommunityAdapter(
    private val mContext: Context,
    private var communityList: MutableList<AppCommunity>
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>(

) {

    class ViewHolder(private val binding: ItemListChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val TAG: String? = CommunityAdapter::class.simpleName
        fun bind(chat: AppCommunity) {

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    if (chat.userPhotoUrl?.let { isUrlWorking(it) } == true) {
                        binding.avCommunityProfile.loadImage(chat.userPhotoUrl) {
                            transformations(CircleCropTransformation())
                        }
                    } else {
                        binding.avCommunityProfile.avatarInitials = chat.userName
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image: ${e.message}")
                }
            }

            binding.apply {
                textViewSender.text = chat.userName
                textViewMessage.text = chat.chatMessage
                textViewTimestamp.text = chat.chatTimestamp?.let { CommonUtils.getDaysAgo(it) }

                imageViewAttachment.isVisible = !chat.chatAttachment.isNullOrEmpty()

                textViewMessage.post {
                    if (textViewMessage.lineCount > 4) {
                        textViewMessage.maxLines = 4
                        textViewMessage.text = "${chat.chatMessage}\nRead More"
                    }
                }

                textViewMessage.setOnClickListener {
                    if (textViewMessage.text.endsWith("Read More")) {
                        textViewMessage.maxLines = Int.MAX_VALUE
                        textViewMessage.text = "${chat.chatMessage}\nRead Less"
                    } else if (textViewMessage.text.endsWith("Read Less")) {
                        textViewMessage.maxLines = 4
                        textViewMessage.text = "${chat.chatMessage}\nRead More"
                    }
                }
            }
        }

        suspend fun isUrlWorking(url: String): Boolean {
            return withContext(Dispatchers.IO) { // Run on IO dispatcher
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).head().build()
                    val response = client.newCall(request).execute()
                    response.isSuccessful
                } catch (e: Exception) {
                    false
                }
            }
        }
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

        holder.bind(chat)




    }

    suspend fun isUrlWorking(url: String): Boolean {
        return withContext(Dispatchers.IO) { // Run on IO dispatcher
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).head().build()
                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }


}