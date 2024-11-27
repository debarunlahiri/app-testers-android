package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.databinding.ActivityCreditsBinding
import com.summitcodeworks.apptesters.models.AddCreditsRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class CreditsActivity : AppCompatActivity(), OnUserEarnedRewardListener {

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    private val TAG: String? = CreditsActivity::class.java.simpleName
    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityCreditsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        viewBinding = ActivityCreditsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@CreditsActivity) {}
            // Load an ad on the main thread.
            runOnUiThread {
                loadAd()
            }
        }

        viewBinding.cvCreditsAd.setOnClickListener {
            rewardedInterstitialAd?.show(this, this)
        }
    }

    private fun loadAd() {
        RewardedInterstitialAd.load(this, getString(R.string.rewarded_interstitial_admob_id),
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { Log.d(TAG, it) }
                    rewardedInterstitialAd = null
                }
            })
    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Log.d(TAG, "User earned reward. " + rewardItem.amount)
        // TODO: Reward the user!
        addCreditsToUser(rewardItem.amount)
    }

    private fun addCreditsToUser(amount: Int) {
        val addCreditsRequest = AddCreditsRequest()
        addCreditsRequest.credits = amount
        RetrofitClient.apiInterface(mContext).addCredits(SharedPrefsManager.getUserDetails(mContext).userId, addCreditsRequest).enqueue(object : Callback<ResponseHandler> {
            override fun onResponse(p0: Call<ResponseHandler>, p1: Response<ResponseHandler>) {
                if (p1.isSuccessful) {
                    if (p1.code() == 200) {
                        p1.body()?.header?.responseMessage?.let {
                            CommonUtils.showToastLong(mContext,
                                it
                            )
                            CommonUtils.authenticateUser(mContext, object : AuthenticationCallback {
                                override fun onSuccess(userDetails: UserDetailsResponse?) {
                                    viewBinding.tvUserDetCredits.text = SharedPrefsManager.getUserDetails(mContext).userCredits.toString()
                                }

                                override fun onError(errorCode: Int, errorMessage: String) {
                                    CommonUtils.showToastLong(mContext, errorMessage)
                                }

                                override fun onFailure(throwable: Throwable) {
                                }

                            })
                        }
                    }
                }
            }

            override fun onFailure(p0: Call<ResponseHandler>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }

        })
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

        viewBinding.tvUserDetCredits.text = SharedPrefsManager.getUserDetails(mContext).userCredits.toString()
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbCredits.navigationIcon = wrappedIcon
        }
    }
}