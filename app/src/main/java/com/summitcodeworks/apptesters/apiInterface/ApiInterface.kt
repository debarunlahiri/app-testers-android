package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.models.UserAppRequest
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.appDetails.AppDetails
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userApps.UserApps
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @POST("register-user")
    fun registerUser(@Body userRequest: UserRequest): Call<ResponseHandler>

    @GET("app-lists")
    fun getAppList(): Call<UserApps>

    @GET("app-detail/{appId}")
    fun getAppDetails(@Path("appId") appId: Int): Call<AppDetails>


    @POST("create-app")
    fun createApp(@Body userAppRequest: UserAppRequest): Call<ResponseHandler>

    @GET("login")
    fun loginUser(): Call<UserDetails>


}