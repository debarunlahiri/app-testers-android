package com.summitcodeworks.apptesters.utils

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import com.summitcodeworks.apptesters.activities.ErrorActivity
import com.summitcodeworks.apptesters.activities.RegisterActivity
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.apiInterface.AppConstantsCallback
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.models.appConstants.AppConstants
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommonUtils {

    companion object {

        fun convertDate(inputString: String): String {
            // Extract the date part from the input string
            val datePattern = """(\w{3}, \d{2} \w{3} \d{4} \d{2}:\d{2}:\d{2} GMT)""".toRegex()
            val matchResult = datePattern.find(inputString)

            // If a match is found, extract the date string
            val dateString = matchResult?.groups?.get(1)?.value

            // Define the input format
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)

            // Parse the date string to a Date object
            val date: Date? = dateString?.let { inputFormat.parse(it) }

            // Define the output format
            val outputFormat = SimpleDateFormat("dd'th' MMM yyyy", Locale.ENGLISH)

            // Check if the date is valid and format it
            return if (date != null) {
                // Get the day to apply the suffix
                val day = SimpleDateFormat("d", Locale.ENGLISH).format(date).toInt()
                val suffix = when {
                    day in 11..13 -> "th"
                    day % 10 == 1 -> "st"
                    day % 10 == 2 -> "nd"
                    day % 10 == 3 -> "rd"
                    else -> "th"
                }

                // Format the date and replace 'th' with the correct suffix
                outputFormat.format(date).replace("th", suffix)
            } else {
                "Invalid Date"
            }
        }

        fun applyBoldStyle(boldPart: String, fullText: String): SpannableString {
            val spannableString = SpannableString(fullText)
            val start = fullText.indexOf(boldPart)
            val end = start + boldPart.length
            spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        }

        fun showToastLong(mContext: Context, message: String) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()

        }

        fun apiRequestFailedToast(mContext: Context, p1: Throwable) {
            Log.e(RegisterActivity.TAG, "Network request failed", p1)
            if (p1 is IOException) {
                Toast.makeText(mContext, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }


        fun authenticateUser(mContext: Context, callback: AuthenticationCallback) {
            RetrofitClient.apiInterface(mContext).authenticateUser().enqueue(object : Callback<UserDetails> {
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    if (response.isSuccessful) {
                        val userDetails = response.body()?.response
                        if (userDetails != null) {
                            SharedPrefsManager.saveUserDetails(mContext, userDetails)
                            callback.onSuccess(userDetails)
                        } else {
                            callback.onError(response.code(), "User details are null")
                        }
                    } else {
                        Log.e(TAG, "Login failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                        callback.onError(response.code(), response.message())
                    }
                }

                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    apiRequestFailedToast(mContext, t)
                    callback.onFailure(t)
                    sendToErrorActivity(mContext)
                }
            })
        }

        fun authenticateUser(mContext: Context) {
            RetrofitClient.apiInterface(mContext).authenticateUser().enqueue(object : Callback<UserDetails> {
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    if (response.isSuccessful) {
                        val userDetails = response.body()?.response
                        if (userDetails != null) {
                            SharedPrefsManager.saveUserDetails(mContext, userDetails)
                        } else {
                        }
                    } else {
                        Log.e(TAG, "Login failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    apiRequestFailedToast(mContext, t)
                    sendToErrorActivity(mContext)
                }
            })
        }


        fun fetchAppConstant(mContext: Context, constantKey: String, callback: AppConstantsCallback) {
            RetrofitClient.apiInterface(mContext).getAppConstants(constantKey).enqueue(object : Callback<AppConstants> {
                override fun onResponse(call: Call<AppConstants>, response: Response<AppConstants>) {
                    if (response.isSuccessful) {
                        if (response.body().header.responseCode == 200) {
                            val appConstantsResponse = response.body()?.response
                            if (appConstantsResponse != null) {
                                callback.onSuccess(appConstantsResponse)
                            } else {
                                callback.onError(response.code(), "User details are null")
                            }
                        }

                    } else {
                        Log.e(TAG, "Login failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                        callback.onError(response.code(), response.message())
                    }
                }

                override fun onFailure(call: Call<AppConstants>, t: Throwable) {
                    apiRequestFailedToast(mContext, t)
                    callback.onFailure(t)
                    sendToErrorActivity(mContext)
                }
            })
        }

        fun sendToErrorActivity(mContext: Context) {
            val errorIntent = Intent(mContext, ErrorActivity::class.java)
            mContext.startActivity(errorIntent)
        }



    }


}