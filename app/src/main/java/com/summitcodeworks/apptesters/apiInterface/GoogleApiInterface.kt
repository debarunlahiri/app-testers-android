package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.fragments.GoogleGroupFragment
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GoogleApiInterface {
    @GET("groups/v1/groups/{groupEmail}/members/{userEmail}")
    suspend fun checkMembership(
        @Path("groupEmail") groupEmail: String,
        @Path("userEmail") userEmail: String
    ): Response<GoogleGroupFragment.MembershipResponse>
}