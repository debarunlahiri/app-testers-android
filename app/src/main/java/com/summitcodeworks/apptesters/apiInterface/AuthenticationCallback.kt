package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse

interface AuthenticationCallback {
    fun onSuccess(userDetails: UserDetailsResponse?)
    fun onError(errorCode: Int, errorMessage: String)
    fun onFailure(throwable: Throwable)
}