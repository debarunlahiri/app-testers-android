package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.models.appConstants.AppConstantsResponse
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse

interface AppConstantsCallback {
    fun onSuccess(appConstantsResponse: AppConstantsResponse)
    fun onError(errorCode: Int, errorMessage: String)
    fun onFailure(throwable: Throwable)
}