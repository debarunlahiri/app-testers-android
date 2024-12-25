package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class ServerStatus(
    @SerializedName("status") private var _status: String = "",
    @SerializedName("timestamp") private var _timestamp: Int = 0
) {
    var status: String
        get() = _status
        set(value) {
            _status = value.uppercase()  // Example: Convert to uppercase when setting
        }

    var timestamp: Int
        get() = _timestamp
        set(value) {
            _timestamp = value
        }

}

