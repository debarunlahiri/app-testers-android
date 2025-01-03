package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class UserAppRequest(
    @SerializedName("appName") private var _appName: String = "",
    @SerializedName("appDevName") private var _appDevName: String = "",
    @SerializedName("appWebLink") private var _appWebLink: String = "",
    @SerializedName("appAppLink") private var _appLink: String = "",
    @SerializedName("appDesc") private var _appDescription: String = "",
    @SerializedName("appLogo") private var _appLogo: String = "",
    @SerializedName("appCreatedBy") private var _userCreatedBy: String = "",
    @SerializedName("appPkgNme") private var _appPkgNme: String = "",
    @SerializedName("appCredit") private var _appCredit: String = ""
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

    var userCreatedBy: String
        get() = _userCreatedBy
        set(value) {
            _userCreatedBy = value
        }

    var appPkgNme: String
        get() = _appPkgNme
        set(value) {
            _appPkgNme = value
        }

    var appCredit: String
        get() = _appCredit
        set(value) {
            _appCredit = value
        }
}

