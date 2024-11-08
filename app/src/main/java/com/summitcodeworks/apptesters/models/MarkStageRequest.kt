package com.summitcodeworks.apptesters.models

import com.google.gson.annotations.SerializedName

data class MarkStageRequest(
    @SerializedName("user_id") private var _userId: Int = 0,
    @SerializedName("app_id") private var _appId: Int = 0,
    @SerializedName("stage_no") private var _stageNo: Int = 0
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
