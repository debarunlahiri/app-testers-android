package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ActivityErrorBinding

class ErrorActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityErrorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mContext = this@ErrorActivity
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



    }
}