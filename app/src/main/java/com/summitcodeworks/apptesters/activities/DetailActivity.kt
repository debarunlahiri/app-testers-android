package com.summitcodeworks.apptesters.activities

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
import com.summitcodeworks.apptesters.models.appDetails.AppDetails
import com.summitcodeworks.apptesters.models.appDetails.AppDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DetailActivity : AppCompatActivity() {

    private var appDetailsResponse: AppDetailsResponse = AppDetailsResponse()
    private lateinit var viewBinding: ActivityDetailBinding
    private var appId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Get the app ID from the intent
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

    private fun checkAndLaunchApp() {
        val pm = packageManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(appDetailsResponse.appPkgNme, 0)
            }

            val launchIntent = pm.getLaunchIntentForPackage(appDetailsResponse.appPkgNme)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
                startActivity(browserIntent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "App not installed, opening link", Toast.LENGTH_SHORT).show()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appDetailsResponse.appAppLink))
            startActivity(browserIntent)
        }
    }

    private fun setupUI() {
        // Set toolbar title
        viewBinding.tbDetail.title = "App Details"

        // Set the toolbar as the support action bar
        setSupportActionBar(viewBinding.tbDetail)

        // Enable the "up" button (back arrow) on the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set the toolbar navigation click listener to close the activity when the back button is pressed
        viewBinding.tbDetail.setNavigationOnClickListener {
            finish() // Closes the current activity and navigates back
        }

        // Load the navigation icon (back arrow) from drawable and set it
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbDetail.setNavigationIcon(navigationIcon)

        // Check for night mode and update toolbar text and icon colors accordingly
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

    // Helper function to apply tint to the navigation icon
    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbDetail.navigationIcon = wrappedIcon
        }
    }

    private fun fetchAppDetails(appId: Int) {
        RetrofitClient.apiInterface.getAppDetails(appId).enqueue(object : Callback<AppDetails> {
            override fun onResponse(call: Call<AppDetails>, response: Response<AppDetails>) {
                if (response.isSuccessful) {
                    response.body()?.let { appDetails ->
                        populateDetails(appDetails.response)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppDetails>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateDetails(appDetailsResponse: AppDetailsResponse) {
        this.appDetailsResponse = appDetailsResponse
        // Check if the app logo is an SVG
        if (appDetailsResponse.appLogo.endsWith(".svg")) {
            loadSvg(appDetailsResponse.appLogo)
        } else {
            // Set app logo using Glide
            Glide.with(this)
                .load(appDetailsResponse.appLogo)
                .placeholder(R.mipmap.ic_launcher) // Placeholder image
                .into(viewBinding.ivAppLogo)
        }

        // Set app name
        viewBinding.tvDetailAppName.text = appDetailsResponse.appName
        viewBinding.tvDetailDesc.text = appDetailsResponse.appDesc

        // Set developer name with bold "Developed by"
        val devByText = "Developed by ${appDetailsResponse.appDevName}"
        viewBinding.tvDetailDevelopBy.text = applyBoldStyle("Developed by", devByText)

        // Set posted on date with bold "Posted on"
        val postedOnText = "Posted on ${CommonUtils.convertDate(appDetailsResponse.appCreatedOn)}"
        viewBinding.tvDetailPostedOn.text = applyBoldStyle("Posted on", postedOnText)

        // Set credits with bold "Created by"
        val creditsText = "Credits ${appDetailsResponse.appCredit}"
        viewBinding.tvDetailCredits.text = applyBoldStyle("Credits ", creditsText)

        updateButtonText()
    }

    private fun updateButtonText() {
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
            viewBinding.bTestNow.text = "Test Now"
        } catch (e: PackageManager.NameNotFoundException) {
            // App is not installed, update button text to "Install Now"
            viewBinding.bTestNow.text = "Install Now"
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
}
