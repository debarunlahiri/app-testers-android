package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.models.AddCreditsRequest
import com.summitcodeworks.apptesters.models.MarkStageRequest
import com.summitcodeworks.apptesters.models.UserAppRequest
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.appDetails.AppDetails
import com.summitcodeworks.apptesters.models.markStage.MarkStage
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userApps.UserApps
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("user/register-user")
    fun registerUser(@Body userRequest: UserRequest): Call<ResponseHandler>

    @POST("app/create-app")
    fun createApp(@Body userAppRequest: UserAppRequest): Call<ResponseHandler>

    @POST("user/authenticate")
    fun authenticateUser(): Call<UserDetails>

    @POST("api/stages")
    fun markStage(@Body markStageRequest: MarkStageRequest): Call<ResponseHandler>



    @GET("api/stages/user/{userId}/{appId}")
    fun getMarkStageByUserId(@Path("userId") userId: Int, @Path("appId") appId: Int): Call<MarkStage>

    @GET("app/app-lists")
    fun getAppList(@Query("page") page: Int, @Query("per_page") perPage: Int): Call<UserApps>

    @GET("app/app-detail/{appId}")
    fun getAppDetails(@Path("appId") appId: Int): Call<AppDetails>

    @GET("user/user-apps")
    fun getUserApps(): Call<UserApps>

    @GET("user/user-tested-apps")
    fun getUserTestedApps(): Call<UserApps>

    @GET("app/search-apps")
    fun searchApps(@Query("app_name") app_name: String): Call<UserApps>



    @PUT("user/add-credits/{userId}")
    fun addCredits(@Path("userId") userId: Int, @Body addCreditsRequest: AddCreditsRequest): Call<ResponseHandler>


}