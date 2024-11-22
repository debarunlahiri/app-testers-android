package com.summitcodeworks.apptesters.activities

import android.content.Context
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
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this@HelpActivity

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
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbHelp.navigationIcon = wrappedIcon
        }
    }
}