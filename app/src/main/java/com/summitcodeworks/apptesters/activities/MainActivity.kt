package com.summitcodeworks.apptesters.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.databinding.ActivityMainBinding
import com.summitcodeworks.apptesters.activities.LoginActivity // Import your LoginActivity
import com.summitcodeworks.apptesters.adapter.ViewPagerAdapter
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.utils.SharedPrefsManager

class MainActivity : AppCompatActivity() {

    private val TAG: String? = MainActivity::class.java.simpleName
    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        mContext = this

        // Enable dark mode based on system settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(viewBinding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is logged in
        checkLoginStatus()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPagerAdapter = ViewPagerAdapter(this)
        viewBinding.vpMain.adapter = viewPagerAdapter

        viewBinding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_main_menu_item -> viewBinding.vpMain.setCurrentItem(0, false)
                R.id.add_main_menu_item -> viewBinding.vpMain.setCurrentItem(1, false)
                R.id.profile_main_menu_item -> viewBinding.vpMain.setCurrentItem(2, false)
                else -> {
                    viewBinding.vpMain.setCurrentItem(0, false)
                }
            }

            true
        }

        viewBinding.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.home_main_menu_item
                    1 -> viewBinding.bottomNavigationView.selectedItemId = R.id.add_main_menu_item
                    2 -> viewBinding.bottomNavigationView.selectedItemId = R.id.profile_main_menu_item
                }
            }
        })


        Log.i(TAG, "onCreate:getUserDetails " + Gson().toJson(SharedPrefsManager.getUserDetails(mContext)))

    }

    private fun checkLoginStatus() {
        checkPermissions()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Optionally call finish to remove MainActivity from the back stack
        } else {
            // User is logged in, you can proceed to show main content
            // For example, update UI with user info
            RetrofitClient.API_KEY = currentUser.uid

        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        // Check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check file permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Request permissions if not granted
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray())
        } else {
            // All permissions are already granted
            // You can proceed with notifications and file access
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        // ActivityResultLauncher for permission request
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, isGranted) ->
                if (isGranted) {
                    // Permission granted
                    // Handle permission granted logic
                } else {
                    // Permission denied
                    // Handle permission denied logic
                }
            }
        }

        // Launch the permission request
        requestPermissionLauncher.launch(permissions)
    }
}
