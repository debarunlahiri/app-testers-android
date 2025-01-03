package com.summitcodeworks.apptesters.utils

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.summitcodeworks.apptesters.R
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class CommonUtils {

    companion object {

        lateinit var appContext: Context

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
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        }

        fun showToastLong(mContext: Context, message: String) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()

        }

        fun apiRequestFailedToast(mContext: Context, p1: Throwable) {
            Log.e(RegisterActivity.TAG, "Network request failed", p1)
            if (p1 is IOException) {
                Toast.makeText(mContext, "Network error. Please try again.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    mContext,
                    "An unexpected error occurred. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        fun authenticateUser(mContext: Context, callback: AuthenticationCallback) {
            RetrofitClient.apiInterface(mContext).authenticateUser()
                .enqueue(object : Callback<UserDetails> {
                    override fun onResponse(
                        call: Call<UserDetails>,
                        response: Response<UserDetails>
                    ) {
                        if (response.isSuccessful) {
                            val userDetails = response.body()?.response
                            if (userDetails != null) {
                                SharedPrefsManager.saveUserDetails(mContext, userDetails)
                                callback.onSuccess(userDetails)
                            } else {
                                callback.onError(response.code(), "User details are null")
                            }
                        } else {
                            Log.e(
                                TAG,
                                "Login failed with code: ${response.code()} - ${response.message()}"
                            )
                            Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                            callback.onError(response.code(), response.message())
                        }
                    }

                    override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                        apiRequestFailedToast(mContext, t)
                        callback.onFailure(t)
                    }
                })
        }

        fun authenticateUser(mContext: Context) {
            RetrofitClient.apiInterface(mContext).authenticateUser()
                .enqueue(object : Callback<UserDetails> {
                    override fun onResponse(
                        call: Call<UserDetails>,
                        response: Response<UserDetails>
                    ) {
                        if (response.isSuccessful) {
                            val userDetails = response.body()?.response
                            if (userDetails != null) {
                                SharedPrefsManager.saveUserDetails(mContext, userDetails)

                            } else {
                            }
                        } else {
                            Log.e(
                                TAG,
                                "Login failed with code: ${response.code()} - ${response.message()}"
                            )
                            Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                        apiRequestFailedToast(mContext, t)
                    }
                })
        }


        fun fetchAppConstant(mContext: Context, appPkg: String, callback: AppConstantsCallback) {
            RetrofitClient.apiInterface(mContext).getAppConstants(appPkg)
                .enqueue(object : Callback<AppConstants> {
                    override fun onResponse(
                        call: Call<AppConstants>,
                        response: Response<AppConstants>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.header?.responseCode == 200) {
                                val appConstantsResponse = response.body()?.response
                                if (appConstantsResponse != null) {
                                    callback.onSuccess(appConstantsResponse)
                                } else {
                                    callback.onError(response.code(), "User details are null")
                                }
                            }

                        } else {
                            Log.e(
                                TAG,
                                "Login failed with code: ${response.code()} - ${response.message()}"
                            )
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

        fun showJoinGroupDialog(context: Context) {
            // Create a LinearLayout for the dialog content
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 40, 40, 40)
                gravity = Gravity.CENTER_HORIZONTAL
            }

            // Add a TextView for the header
            val headerTextView = TextView(context).apply {
                text = "Join our Group"
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 20) // Padding below the header
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(headerTextView)

            // Add a LottieAnimationView for the animation
            val animationView = LottieAnimationView(context).apply {
                // Load Lottie animation from raw resources
                val inputStream: InputStream =
                    context.resources.openRawResource(R.raw.group) // group.json is in res/raw folder
                val json = inputStream.bufferedReader().use { it.readText() }

                // Set the animation data
                setAnimationFromJson(json, "group_animation")

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400 // Adjust height as needed
                ).apply {
                    bottomMargin = 20 // Space between animation and text
                }
                repeatCount = LottieDrawable.INFINITE // Infinite loop for animation
                playAnimation() // Start the animation
            }
            layout.addView(animationView)

            // Add a TextView for the message
            val textView = TextView(context).apply {
                text =
                    "To ensure maximum visibility for your app, please join our group before posting. Apps shared without group membership will not be visible to other users."
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 20, 0, 20)
            }
            layout.addView(textView)

            // Set the layout as the view for the dialog
            val dialogBuilder = AlertDialog.Builder(context).apply {
                setView(layout)
                setCancelable(false) // Make dialog non-cancelable by tapping outside
                setPositiveButton("Join Now") { _, _ ->
                    SharedPrefsManager.setGroupPopup(context, true)
                    // Open the group URL when "Join Now" is clicked
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data =
                            Uri.parse("https://groups.google.com/g/app-testers-community-summitcodeworks")
                    }
                    context.startActivity(intent)
                }
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            }

            // Customize dialog to use the rounded background
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)


            dialog.show()
        }

        fun formatDate(inputDate: String): String {
            // Parse the ISO-8601 date string
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)

            val date: Date = inputFormat.parse(inputDate) ?: return ""
            val formattedDate = outputFormat.format(date)

            // Add ordinal suffix to the day
            val day = SimpleDateFormat("d", Locale.ENGLISH).format(date).toInt()
            val dayWithSuffix = when {
                day in 11..13 -> "${day}th"
                day % 10 == 1 -> "${day}st"
                day % 10 == 2 -> "${day}nd"
                day % 10 == 3 -> "${day}rd"
                else -> "${day}th"
            }

            val monthYear = SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(date)

            return "$dayWithSuffix $monthYear"
        }

        fun extractPackageName(url: String): String? {
            return try {
                val uri = Uri.parse(url)
                uri.getQueryParameter("id") // Extracts the 'id' query parameter
            } catch (e: Exception) {
                null // Return null if extraction fails
            }
        }

        fun getDaysAgo(timestamp: String): String {
            return try {
                // Define the date format
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")

                // Parse the timestamp
                val date = formatter.parse(timestamp) ?: return "Invalid date"

                // Get current date
                val now = Date()

                // Calculate difference in milliseconds
                val diffInMillis = now.time - date.time

                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis) % 60

                // Build the result string
                return when {
                    days > 0 -> "$days days, $hours hours ago"
                    hours > 0 -> "$hours hours, $minutes minutes ago"
                    minutes > 0 -> "$minutes minutes, $seconds seconds ago"
                    seconds > 0 -> "$seconds seconds ago"
                    else -> "Just now"
                }
            } catch (e: Exception) {
                "Invalid date format"
            }
        }

    }





}