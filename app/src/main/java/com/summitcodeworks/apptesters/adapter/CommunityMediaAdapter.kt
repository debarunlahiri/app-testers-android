package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.summitcodeworks.apptesters.databinding.ItemListCommunityMediaLayoutBinding
import com.summitcodeworks.apptesters.models.CommunityMedia

class CommunityMediaAdapter(private val mContext: Context, private val communityMediaList: MutableList<CommunityMedia>, private val onCommunityMediaListener: OnCommunityMediaListener): RecyclerView.Adapter<CommunityMediaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemListCommunityMediaLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListCommunityMediaLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return communityMediaList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val communityMedia = communityMediaList[position]

        Glide.with(mContext).load(communityMedia.mediaUri).into(holder.binding.ivCommunityMedia)

        holder.binding.cvCommunityMedia.setOnClickListener {
            onCommunityMediaListener.onCommunityMediaClick(communityMedia, position)
        }

        holder.binding.cvCommunityMediaDelete.setOnClickListener {
            onCommunityMediaListener.onCommunityMediaDelete(communityMedia, position)
        }

        holder.binding.ivCommunityMediaDelete.setOnClickListener {
            onCommunityMediaListener.onCommunityMediaDelete(communityMedia, position)
        }
    }

    interface OnCommunityMediaListener {
        fun onCommunityMediaClick(communityMedia: CommunityMedia, position: Int)
        fun onCommunityMediaDelete(communityMedia: CommunityMedia, position: Int)
    }
}