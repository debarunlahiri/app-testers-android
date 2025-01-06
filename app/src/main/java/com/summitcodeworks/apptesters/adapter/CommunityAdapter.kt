package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.databinding.ItemListChatLayoutBinding
import com.summitcodeworks.apptesters.models.AppCommunity
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.CommunityDiffCallback
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
) : ListAdapter<AppCommunity, CommunityAdapter.ViewHolder>(
    CommunityDiffCallback()
) {
    val TAG: String? = CommunityAdapter::class.simpleName

    class ViewHolder(private val binding: ItemListChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        suspend fun bind(mContext: Context, chat: AppCommunity) {
            val TAG: String = CommunityAdapter::class.simpleName + "ViewHolder"
            binding.apply {
                textViewSender.text = chat.userName ?: "Unknown"
                textViewMessage.text = chat.chatMessage ?: "No message"
                textViewMessage.visibility = if (chat.chatMessage.isEmpty()) View.GONE else View.VISIBLE
                textViewTimestamp.text = chat.chatTimestamp?.let { CommonUtils.getDaysAgo(it) }

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

                includeCommunityMediaPreview.ivCommunityMediaPreviewOne.visibility = View.GONE
                includeCommunityMediaPreview.ivCommunityMediaPreviewTwo.visibility = View.GONE
                includeCommunityMediaPreview.ivCommunityMediaPreviewThree.visibility = View.GONE
                includeCommunityMediaPreview.ivCommunityMediaPreviewFour.visibility = View.GONE
                cvCommunityMediaPreview.visibility = View.GONE

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

                if (chat.mediaList.isNotEmpty()) {
                    cvCommunityMediaPreview.visibility = View.VISIBLE

                    chat.mediaList.forEachIndexed { index, media ->
                        when (index) {
                            0 -> loadImageIntoView(mContext, media.mediaUrl, includeCommunityMediaPreview.ivCommunityMediaPreviewOne)
                            1 -> loadImageIntoView(mContext, media.mediaUrl, includeCommunityMediaPreview.ivCommunityMediaPreviewTwo)
                            2 -> loadImageIntoView(mContext, media.mediaUrl, includeCommunityMediaPreview.ivCommunityMediaPreviewThree)
                            3 -> loadImageIntoView(mContext, media.mediaUrl, includeCommunityMediaPreview.ivCommunityMediaPreviewFour)
                        }
                    }
                }

                cvCommunityMediaPreview.setOnClickListener {
                    StfalconImageViewer.Builder(mContext, chat.mediaList) { view, media ->
                        Glide.with(mContext).load(media.mediaUrl).into(view)
                    }.withStartPosition(0).show()
                }


            }
        }

        suspend fun isUrlWorking(url: String): Boolean {
            return withContext(Dispatchers.IO) {
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

        private fun loadImageIntoView(context: Context, url: String?, imageView: View) {
            if (url.isNullOrEmpty()) {
                imageView.visibility = View.GONE
            } else {
                imageView.visibility = View.VISIBLE
                Glide.with(context)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView as android.widget.ImageView)
            }
        }
    }

    @JvmName("submitList1")
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

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = communityList[position]

        CoroutineScope(Dispatchers.Main).launch {
            try {
                holder.bind(mContext, chat)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}")
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


