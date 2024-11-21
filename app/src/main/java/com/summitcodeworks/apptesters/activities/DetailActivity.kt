package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.ActivityDetailBinding
import com.summitcodeworks.apptesters.models.MarkStageRequest
import com.summitcodeworks.apptesters.models.appDetails.AppDetails
import com.summitcodeworks.apptesters.models.appDetails.AppDetailsResponse
import com.summitcodeworks.apptesters.models.markStage.MarkStage
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import io.getstream.avatarview.coil.loadImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DetailActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private var appDetailsResponse: AppDetailsResponse = AppDetailsResponse()
    private lateinit var viewBinding: ActivityDetailBinding
    private var appId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this


        viewBinding.bDetailAppLink.setOnClickListener {
            checkAndLaunchApp(true)
        }

    }

    private fun checkAndLaunchApp(isExternalClick: Boolean = false) {
        val pm = packageManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(appDetailsResponse.appPkgNme, 0)
            }

            val launchIntent = pm.getLaunchIntentForPackage(appDetailsResponse.appPkgNme)

            if (isExternalClick) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
                startActivity(browserIntent)
            } else {
                val buttonText = viewBinding.bTestNow.text.toString()
                when (buttonText) {
                    getString(R.string.install_app) -> {
                        markStageNo(1)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
                        startActivity(browserIntent)
                    }
                    getString(R.string.test_now) -> {
                        if (launchIntent != null) {
                            markStageNo(2)
                            startActivity(launchIntent)
                        } else {
                            CommonUtils.showToastLong(mContext, "App is not available")
                        }
                    }
                    getString(R.string.give_feedback) -> {
                        markStageNo(3)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
                        startActivity(browserIntent)
                    }
                    getString(R.string.open_app) -> {
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            CommonUtils.showToastLong(mContext, "App is not available")
                        }
                    }
                }
            }


        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "App not installed, opening link", Toast.LENGTH_SHORT).show()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
            startActivity(browserIntent)

        }
    }

    private fun markStageNo(stageNo: Int) {
        var markStageRequest = MarkStageRequest()
        markStageRequest.userId = SharedPrefsManager.getUserDetails(mContext).userId
        markStageRequest.stageNo = stageNo
        markStageRequest.appId = appId

        RetrofitClient.apiInterface(mContext).markStage(markStageRequest).enqueue(object : Callback<ResponseHandler> {
            override fun onResponse(p0: Call<ResponseHandler>, p1: Response<ResponseHandler>) {
                if (p1.isSuccessful) {
                    if (p1.code() == 201) {
                        updateButtonText()

                    }
                }
            }

            override fun onFailure(p0: Call<ResponseHandler>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }

        })
    }

    private fun setupUI() {
        viewBinding.pbDetail.visibility = View.VISIBLE
        viewBinding.tbDetail.title = "App Details"

        setSupportActionBar(viewBinding.tbDetail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbDetail.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbDetail.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbDetail.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbDetail.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbDetail.navigationIcon = wrappedIcon
        }
    }

    private fun fetchAppDetails(appId: Int) {
        RetrofitClient.apiInterface(mContext).getAppDetails(appId).enqueue(object : Callback<AppDetails> {
            override fun onResponse(call: Call<AppDetails>, response: Response<AppDetails>) {
                if (response.isSuccessful) {
                    hideProgress()
                    response.body()?.let { appDetails ->
                        populateDetails(appDetails.response)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppDetails>, t: Throwable) {
                hideProgress()
                CommonUtils.apiRequestFailedToast(mContext, t)
            }
        })
    }

    private fun populateDetails(appDetailsResponse: AppDetailsResponse) {
        this.appDetailsResponse = appDetailsResponse
        if (appDetailsResponse.appLogo.endsWith(".svg")) {
            loadSvg(appDetailsResponse.appLogo)
        } else {
            // Set app logo using Glide
            Glide.with(this)
                .load(appDetailsResponse.appLogo)
                .placeholder(R.mipmap.ic_launcher)
                .into(viewBinding.ivAppLogo)
        }

        // Set app name
        viewBinding.tvDetailAppName.text = appDetailsResponse.appName
        viewBinding.tvDetailDesc.text = appDetailsResponse.appDesc

        // Set developer name with bold "Developed by"
        val devByText = "Developed by: ${appDetailsResponse.appDevName}"
//        viewBinding.tvDetailDeveloperName.text = applyBoldStyle("Developed by: ", devByText)
        viewBinding.tvDetailDeveloperName.text = appDetailsResponse.appDevName

        if (SharedPrefsManager.getUserDetails(mContext).userPhotoUrl != null) {
            viewBinding.avDevProfileImage.loadImage(SharedPrefsManager.getUserDetails(mContext).userPhotoUrl)
        } else {
            viewBinding.avDevProfileImage.avatarInitials = appDetailsResponse.appDevName
        }

        val postedOnText = "Posted on: ${CommonUtils.convertDate(appDetailsResponse.appCreatedOn)}"
        viewBinding.tvDetailPostedOn.text = applyBoldStyle("Posted on: ", postedOnText)

        val creditsText = "Credits: ${appDetailsResponse.appCredit}"
        viewBinding.tvDetailCredits.text = applyBoldStyle("Credits: ", creditsText)

        updateButtonText()
    }

    private fun updateButtonText() {
        RetrofitClient.apiInterface(mContext).getMarkStageByUserId(SharedPrefsManager.getUserDetails(mContext).userId, appId).enqueue(object : Callback<MarkStage> {
            override fun onResponse(call: Call<MarkStage>, response: Response<MarkStage>) {
                if (response.isSuccessful) {
                    if (response.code() == 200) {
                        if (response.body()?.response?.stageId == null) {
                            checkAppInstalledOrNot()
                        } else {
                            val markStageResponse = response.body()?.response
                            if (markStageResponse != null) {
                                if (markStageResponse.stageNo == 1) {
                                    viewBinding.bTestNow.text = getString(R.string.test_now)
                                } else if (markStageResponse.stageNo == 2) {
                                    viewBinding.bTestNow.text = getString(R.string.give_feedback)
                                } else if (markStageResponse.stageNo == 3) {
                                    viewBinding.bTestNow.text = getString(R.string.open_app)
                                }
                            } else {
                                viewBinding.bTestNow.text = getString(R.string.install_app)
                            }
                        }
                    } else if (response.code() == 404) {
                        checkAppInstalledOrNot()
                    }
                }
            }

            override fun onFailure(p0: Call<MarkStage>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }

        })

    }

    private fun checkAppInstalledOrNot() {
        val pm = packageManager
        try {
            // API check for Android 13 (API level 33) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(appDetailsResponse.appPkgNme, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(appDetailsResponse.appPkgNme, 0)
            }

            // App is installed, update button text to "Test Now"
            viewBinding.bTestNow.text = getString(R.string.test_now)
        } catch (e: PackageManager.NameNotFoundException) {
            viewBinding.bTestNow.text = getString(R.string.install_app)
        }
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
                runOnUiThread {
                    viewBinding.ivAppLogo.setImageDrawable(drawable)
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Error loading SVG: ${e.message}")
            }
        }.start()
    }

    // Helper function to apply bold style to a specific part of the text
    private fun applyBoldStyle(boldPart: String, fullText: String): SpannableString {
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(boldPart)
        val end = start + boldPart.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun showProgress() {
        viewBinding.nsvDetail.visibility = View.GONE
        viewBinding.bTestNow.visibility = View.GONE
        viewBinding.pbDetail.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        viewBinding.nsvDetail.visibility = View.VISIBLE
        viewBinding.bTestNow.visibility = View.VISIBLE
        viewBinding.pbDetail.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        showProgress()
        appId = intent.getIntExtra("app_id", 0)

        setupUI()
        if (appId != 0) {
            fetchAppDetails(appId)

            viewBinding.bTestNow.setOnClickListener {
                if (packageName.isNotEmpty()) {
                    checkAndLaunchApp()
                } else {
                    Toast.makeText(this, "App package not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Invalid App ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
