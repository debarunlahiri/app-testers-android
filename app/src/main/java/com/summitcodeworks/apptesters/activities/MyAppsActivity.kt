package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.adapter.HomeAdapter
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.ActivityMyAppsBinding
import com.summitcodeworks.apptesters.models.userApps.UserApps
import com.summitcodeworks.apptesters.models.userApps.UserAppsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAppsActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityMyAppsBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMyAppsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this

        setupUI()


        fetchUserApps()
        fetchTestedApps()

    }

    private fun fetchTestedApps() {
        RetrofitClient.apiInterface(mContext).getUserTestedApps().enqueue(object : Callback<UserApps> {
            override fun onResponse(p0: Call<UserApps>, p1: Response<UserApps>) {
                if (p1.isSuccessful) {
                    val userApps = p1.body()
                    if (userApps != null) {
                        if (userApps.response.isEmpty()) {
                            viewBinding.tvNoTestedApps.visibility = View.VISIBLE
                            viewBinding.rvTestedApps.visibility = View.GONE
                        } else {
                            viewBinding.tvNoUserApps.visibility = View.GONE
                            viewBinding.rvTestedApps.visibility = View.VISIBLE
                            val homeAdapter = HomeAdapter(mContext, userApps.response, object : HomeAdapter.OnHomeAdapterListener {
                                override fun onHomeAdapterClick(userApps: UserAppsResponse) {
                                    openDetailPage(userApps)
                                }
                            })
                            viewBinding.rvTestedApps.adapter = homeAdapter
                            viewBinding.rvTestedApps.setHasFixedSize(true)
                            viewBinding.rvTestedApps.layoutManager = LinearLayoutManager(mContext)
                            viewBinding.rvTestedApps.isNestedScrollingEnabled = false
                        }
                    }
                }
            }

            override fun onFailure(p0: Call<UserApps>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }
        })
    }

    private fun fetchUserApps() {
        RetrofitClient.apiInterface(mContext).getUserApps().enqueue(object : Callback<UserApps> {
            override fun onResponse(p0: Call<UserApps>, p1: Response<UserApps>) {
                if (p1.isSuccessful) {
                    val userApps = p1.body()
                    if (userApps != null) {
                        if (userApps.header.responseCode == 200) {
                            if (userApps.response.isNullOrEmpty()) {
                                viewBinding.tvNoUserApps.visibility = View.VISIBLE
                                viewBinding.rvUserApps.visibility = View.GONE
                            } else {
                                viewBinding.tvNoUserApps.visibility = View.GONE
                                viewBinding.rvUserApps.visibility = View.VISIBLE
                                val homeAdapter = HomeAdapter(mContext, userApps.response, object : HomeAdapter.OnHomeAdapterListener {
                                    override fun onHomeAdapterClick(userApps: UserAppsResponse) {
                                        openDetailPage(userApps)
                                    }
                                })
                                viewBinding.rvUserApps.adapter = homeAdapter
                                viewBinding.rvUserApps.setHasFixedSize(true)
                                viewBinding.rvUserApps.layoutManager = LinearLayoutManager(mContext)
                                viewBinding.rvUserApps.isNestedScrollingEnabled = false
                            }
                        }
                    }
                }
            }

            override fun onFailure(p0: Call<UserApps>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }
        })
    }

    private fun setupUI() {
        viewBinding.tbMyApps.title = "My Apps"
        setSupportActionBar(viewBinding.tbMyApps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbMyApps.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbMyApps.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbMyApps.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbMyApps.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }

    }


    private fun openDetailPage(userApps: UserAppsResponse) {
        val detailIntent = Intent(mContext, DetailActivity::class.java)
        detailIntent.putExtra("app_id", userApps.appId)
        startActivity(detailIntent)
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbMyApps.navigationIcon = wrappedIcon
        }
    }
}