package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ActivityCreditsBinding

class CreditsActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityCreditsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        viewBinding = ActivityCreditsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()


    }

    private fun setupUI() {
        viewBinding.tbCredits.title = "Your Credits"
        setSupportActionBar(viewBinding.tbCredits)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbCredits.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbCredits.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbCredits.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbCredits.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbCredits.navigationIcon = wrappedIcon
        }
    }
}