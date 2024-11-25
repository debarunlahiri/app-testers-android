package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.databinding.ActivityHelpBinding
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils

class HelpActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityHelpBinding

    private var supportUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this@HelpActivity

        supportUrl = intent.getStringExtra("support_url") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()




    }


    private fun setupUI() {
        viewBinding.tbHelp.visibility = View.VISIBLE
        viewBinding.tbHelp.title = "Help"

        setSupportActionBar(viewBinding.tbHelp)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbHelp.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbHelp.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbHelp.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbHelp.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }

        setupWebView()


    }

    private fun setupWebView() {
        viewBinding.wvHelp.apply {
            settings.javaScriptEnabled = true // Enable JavaScript
            settings.domStorageEnabled = true // Enable DOM storage for modern web designs
            settings.loadWithOverviewMode = true // Load pages in overview mode
            settings.useWideViewPort = true // Enable viewport adjustments for responsive web design
            settings.builtInZoomControls = true // Enable zoom controls
            settings.displayZoomControls = false // Hide zoom control buttons
            settings.allowFileAccess = true // Enable file access for local resources

            // Set a WebViewClient to handle navigation within the WebView
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false // Ensure links are loaded within the WebView
                }
            }
        }

        // Load the URL from the intent
        if (supportUrl.isNotEmpty()) {
            viewBinding.wvHelp.loadUrl(supportUrl)
        } else {
            viewBinding.wvHelp.loadUrl("https://example.com") // Default URL
        }
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbHelp.navigationIcon = wrappedIcon
        }
    }

    override fun onBackPressed() {
        if (viewBinding.wvHelp.canGoBack()) {
            viewBinding.wvHelp.goBack()
        } else {
            super.onBackPressed()
        }
    }
}