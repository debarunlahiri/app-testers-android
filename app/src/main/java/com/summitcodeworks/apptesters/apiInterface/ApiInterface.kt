package com.summitcodeworks.apptesters.apiInterface

import com.summitcodeworks.apptesters.models.AddCreditsRequest
import com.summitcodeworks.apptesters.models.ChatMessage
import com.summitcodeworks.apptesters.models.MarkStageRequest
import com.summitcodeworks.apptesters.models.Role
import com.summitcodeworks.apptesters.models.UserAppRequest
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.appConstants.AppConstants
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

    @POST("api/users/register-user")
    fun registerUser(@Body userRequest: UserRequest): Call<ResponseHandler>

    @POST("api/app/create-app")
    fun createApp(@Body userAppRequest: UserAppRequest): Call<ResponseHandler>

    @POST("api/users/authenticate")
    fun authenticateUser(): Call<UserDetails>

    @POST("api/stages")
    fun markStage(@Body markStageRequest: MarkStageRequest): Call<ResponseHandler>



    @GET("api/stages/user/{userId}/{appId}")
    fun getMarkStageByUserId(@Path("userId") userId: Int, @Path("appId") appId: Int): Call<MarkStage>

    @GET("api/app/app-lists")
    fun getAppList(@Query("page") page: Int, @Query("per_page") perPage: Int): Call<UserApps>

    @GET("api/app/app-detail/{appId}")
    fun getAppDetails(@Path("appId") appId: Int): Call<AppDetails>

    @GET("api/users/user-apps")
    fun getUserApps(): Call<UserApps>

    @GET("api/users/user-tested-apps")
    fun getUserTestedApps(): Call<UserApps>

    @GET("api/app/search-apps")
    fun searchApps(@Query("app_name") app_name: String): Call<UserApps>


    @GET("api/constants/{app_pkg}")
    fun getAppConstants(@Path("app_pkg") app_pkg: String): Call<AppConstants>



    @PUT("api/users/add-credits/{userId}")
    fun addCredits(@Path("userId") userId: Int, @Body addCreditsRequest: AddCreditsRequest): Call<ResponseHandler>


    @POST("api/chat/send")
    fun sendMessage(@Body chatMessage: ChatMessage): Call<ChatMessage>

    @GET("api/chat/messages")
    fun getMessages(
        @Query("senderId") senderId: Int,
        @Query("receiverId") receiverId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<ChatMessage>>

    @POST("api/roles/save")
    fun saveRole(@Body role: Role): Call<Role>

    @GET("api/roles/list")
    fun getRoles(
        @Query("userId") userId: Int?,
        @Query("appPkg") appPkg: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<Role>>

    @GET("api/roles/user/{userId}")
    fun getRolesByUserId(@Path("userId") userId: Int): Call<List<Role>>

    @GET("api/roles/role-detail")
    fun getRolesByUserIdAndAppPkg(
        @Query("userId") userId: Int,
        @Query("appPkg") appPkg: String
    ): Call<List<Role>>

    @GET("api/roles/user-roles")
    fun getRolesByAppPkg(
        @Query("appPkg") appPkg: String
    ): Call<List<Role>>
}