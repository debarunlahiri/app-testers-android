package com.summitcodeworks.apptesters.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
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



    }


}