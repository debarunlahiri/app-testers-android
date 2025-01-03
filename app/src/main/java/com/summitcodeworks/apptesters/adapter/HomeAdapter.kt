package com.summitcodeworks.apptesters.adapter

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ItemListAppLayoutBinding
import com.summitcodeworks.apptesters.models.userApps.UserAppsResponse
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
        private val TAG: String? = HomeAdapter::class.simpleName
//        https://firebasestorage.googleapis.com/v0/b/app-testers-6b94a.appspot.com/o/app_logos%2F53844d64-47a9-45bd-848e-b1207769cf10.jpg?alt=media&token=d7d3de33-8e3a-46e5-9d16-9b35a45656c1
        // You can create methods here to bind data to your views
        fun bind(userApp: UserAppsResponse, onHomeAdapterListener: OnHomeAdapterListener) {
            // Bind your data to the views using binding
            binding.tvAppName.text = userApp.appName // Example: assuming you have a TextView for app name
            binding.tvAppDevName.text = userApp.appDevName // Example: assuming you have a TextView for dev name
            binding.tvAppCredits.text = "${userApp.appCredit} credits"
            binding.tvAppDesc.text = userApp.appDesc
            // Check if the logo URL ends with .svg
//            binding.avAppLogo.avatarInitials = userApp.appName
//            Glide.with(binding.root.context)
//                .load(userApp.appLogo)
//                .placeholder(R.mipmap.ic_launcher)
//                .error(binding.avAppLogo.avatarInitials) // Optional placeholder
//                .into(binding.avAppLogo)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    if (isUrlWorking(userApp.appLogo)) {
                        binding.avAppLogo.loadImage(userApp.appLogo) {
                            transformations(CircleCropTransformation())
                        }
                    } else {
                        binding.avAppLogo.avatarInitials = userApp.appName
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image: ${e.message}")
                }
            }


            binding.cvHomeApp.setOnClickListener {
                onHomeAdapterListener.onHomeAdapterClick(userApp)
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
