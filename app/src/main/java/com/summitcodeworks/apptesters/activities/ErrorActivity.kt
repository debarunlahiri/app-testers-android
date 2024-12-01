package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ActivityErrorBinding
import java.util.logging.Handler

class ErrorActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityErrorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mContext = this@ErrorActivity
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.retryButton).apply {
            setOnClickListener {
                // Add button animation
                animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction {
                                // Handle retry logic here
                                Toast.makeText(
                                    this@ErrorActivity,
                                    "Retrying...",
                                    Toast.LENGTH_SHORT
                                ).show()
                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    val mainIntent = Intent(mContext, MainActivity::class.java)
                                    startActivity(mainIntent)
                                    finish()
                                }, 1000)
                            }
                    }
            }
        }

//        viewBinding.retryButton.setOnClickListener {
//            val mainIntent = Intent(mContext, MainActivity::class.java)
//            startActivity(mainIntent)
//            finish()
//        }



    }
}