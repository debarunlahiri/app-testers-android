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
import retrofit2.http.Query

interface ApiInterface {

    @POST("register-user")
    fun registerUser(@Body userRequest: UserRequest): Call<ResponseHandler>

    @GET("/app-lists")
    fun getAppList(@Query("page") page: Int, @Query("per_page") perPage: Int): Call<UserApps>


    @GET("app-detail/{appId}")
    fun getAppDetails(@Path("appId") appId: Int): Call<AppDetails>


    @POST("create-app")
    fun createApp(@Body userAppRequest: UserAppRequest): Call<ResponseHandler>

    @POST("authenticate")
    fun authenticateUser(): Call<UserDetails>


}