package com.summitcodeworks.apptesters.utils

import android.content.Context
import android.content.SharedPreferences
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse

object SharedPrefsManager {

    private const val PREF_NAME = "AppTestersPrefs"
    private const val KEY_USER_KEY = "user_key"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_CREDITS = "user_credits"
    private const val KEY_USER_CREATION_DATE = "user_creation_date"
    private const val KEY_USE_FLAG = "use_flag"
    private const val KEY_USER_LOGGED_IN = "user_logged_in"
    private const val APP_POLICY_URL = "app_policy_url"
    private const val TERMS_AND_CONDITIONS_URL = "terms_and_conditions_url"
    private const val SUPPORT_URL = "support_url"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save user details
    fun saveUserDetails(context: Context, userDetails: UserDetailsResponse) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USER_KEY, userDetails.userKey)
        editor.putString(KEY_USER_NAME, userDetails.userName)
        editor.putString(KEY_USER_EMAIL, userDetails.userEmail)
        editor.putInt(KEY_USER_ID, userDetails.userId ?: 0)
        editor.putInt(KEY_USER_CREDITS, userDetails.userCredits ?: 0)
        editor.putString(KEY_USER_CREATION_DATE, userDetails.userCreationDate)
        editor.putBoolean(KEY_USE_FLAG, userDetails.useFlag ?: false)
        editor.putBoolean(KEY_USER_LOGGED_IN, true)
        editor.apply()
    }

    // Get user details
    fun getUserDetails(context: Context): UserDetailsResponse {
        val prefs = getSharedPreferences(context)
        val userDetails = UserDetailsResponse()
        userDetails.userKey = prefs.getString(KEY_USER_KEY, "")
        userDetails.userName = prefs.getString(KEY_USER_NAME, "")
        userDetails.userEmail = prefs.getString(KEY_USER_EMAIL, "")
        userDetails.userId = prefs.getInt(KEY_USER_ID, 0)
        userDetails.userCredits = prefs.getInt(KEY_USER_CREDITS, 0)
        userDetails.userCreationDate = prefs.getString(KEY_USER_CREATION_DATE, "")
        userDetails.useFlag = prefs.getBoolean(KEY_USE_FLAG, false)
        return userDetails
    }

    // Check if the user is logged in
    fun isUserLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_USER_LOGGED_IN, false)
    }

    // Clear user details (log out)
    fun clearUserDetails(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }


    fun getAppPolicyUrl(context: Context): String {
        return getSharedPreferences(context).getString(APP_POLICY_URL, "") ?: ""
    }

    fun setAppPolicyUrl(context: Context, url: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(APP_POLICY_URL, url)
        editor.apply()
    }

    fun getTermsAndConditionsUrl(context: Context): String {
        return getSharedPreferences(context).getString(TERMS_AND_CONDITIONS_URL, "") ?: ""
    }

    fun setTermsAndConditionsUrl(context: Context, url: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(TERMS_AND_CONDITIONS_URL, url)
        editor.apply()
    }

    fun getSupportUrl(context: Context): String {
        return getSharedPreferences(context).getString(SUPPORT_URL, "") ?: ""
    }

    fun setSupportUrl(context: Context, url: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(SUPPORT_URL, url)
        editor.apply()
    }
}
