package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class MarkStageRequest(
    @SerializedName("userId") private var _userId: Int = 0,
    @SerializedName("appId") private var _appId: Int = 0,
    @SerializedName("stageNo") private var _stageNo: Int = 0
) {
    var userId: Int
        get() = _userId
        set(value) {
            _userId = value
        }

    var appId: Int
        get() = _appId
        set(value) {
            _appId = value
        }

    var stageNo: Int
        get() = _stageNo
        set(value) {
            _stageNo = value
        }
}
