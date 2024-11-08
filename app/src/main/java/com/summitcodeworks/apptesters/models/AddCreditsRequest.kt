package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class AddCreditsRequest(
    @SerializedName("credits") private var _credits: Int = 0,
) {
    var credits: Int
        get() = _credits
        set(value) {
            _credits = value
        }
}
