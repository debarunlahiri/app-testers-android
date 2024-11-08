package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ItemListAppLayoutBinding
import com.summitcodeworks.apptesters.models.userApps.UserAppsResponse
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HomeAdapter(
    private val mContext: Context,
    private var userAppsList: List<UserAppsResponse>,
    private val onHomeAdapterListener: OnHomeAdapterListener
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {


    fun setUserAppList(userAppsList: List<UserAppsResponse>) {
        this.userAppsList = userAppsList
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemListAppLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        // You can create methods here to bind data to your views
        fun bind(userApp: UserAppsResponse, onHomeAdapterListener: OnHomeAdapterListener) {
            // Bind your data to the views using binding
            binding.tvAppName.text = userApp.appName // Example: assuming you have a TextView for app name
            binding.tvAppDevName.text = userApp.appDevName // Example: assuming you have a TextView for dev name
            binding.tvAppCredits.text = "${userApp.appCredit} credits"
            binding.tvAppDesc.text = userApp.appDesc
            // Check if the logo URL ends with .svg
            if (userApp.appLogo.endsWith(".svg")) {
                loadSvg(userApp.appLogo)
            } else {
                Glide.with(binding.root.context)
                    .load(userApp.appLogo)
                    .placeholder(R.mipmap.ic_launcher) // Optional placeholder
                    .into(binding.ivAppLogo)
            }
            binding.cvHomeApp.setOnClickListener {
                onHomeAdapterListener.onHomeAdapterClick(userApp)
            }
            // Bind other views as necessary
        }

        private fun loadSvg(url: String) {
            Thread {
                try {
                    // Open a connection to the SVG URL
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()

                    // Get the input stream and parse the SVG
                    val input: InputStream = connection.inputStream
                    val svg = SVG.getFromInputStream(input)
                    val drawable = PictureDrawable(svg.renderToPicture())

                    // Update UI on the main thread
                    binding.ivAppLogo.post {
                        binding.ivAppLogo.setImageDrawable(drawable)
                    }
                } catch (e: Exception) {
                    Log.e("HomeAdapter", "Error loading SVG: ${e.message}")
                }
            }.start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListAppLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userAppsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userApps = userAppsList[position]
        holder.bind(userApps, onHomeAdapterListener) // Bind the data to the ViewHolder
    }



    interface OnHomeAdapterListener {
        fun onHomeAdapterClick(userApps: UserAppsResponse)
    }
}
