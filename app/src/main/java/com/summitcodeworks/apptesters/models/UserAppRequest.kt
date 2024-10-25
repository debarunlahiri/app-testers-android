package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class UserAppRequest(
    @SerializedName("app_name") private var _appName: String = "",
    @SerializedName("app_dev_name") private var _appDevName: String = "",
    @SerializedName("app_web_link") private var _appWebLink: String = "",
    @SerializedName("app_app_link") private var _appLink: String = "",
    @SerializedName("app_desc") private var _appDescription: String = "",
    @SerializedName("app_logo") private var _appLogo: String = ""
) {
    var appName: String
        get() = _appName
        set(value) {
            _appName = value.uppercase()  // Example: Convert to uppercase when setting
        }

    var appDevName: String
        get() = _appDevName
        set(value) {
            _appDevName = value
        }

    var appWebLink: String
        get() = _appWebLink
        set(value) {
            _appWebLink = value
        }

    var appLink: String
        get() = _appLink
        set(value) {
            _appLink = value
        }

    var appDescription: String
        get() = _appDescription
        set(value) {
            _appDescription = value
        }

    var appLogo: String
        get() = _appLogo
        set(value) {
            _appLogo = value
        }
}

