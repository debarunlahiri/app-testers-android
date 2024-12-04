package com.summitcodeworks.apptesters.apiClient

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.summitcodeworks.apptesters.apiInterface.GoogleApiInterface
import com.summitcodeworks.apptesters.utils.CommonUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleRetrofitClient {
    private const val BASE_URL = "https://www.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
//        .addInterceptor(ChuckerInterceptor(CommonUtils.appContext))
        .build()

    val instance: GoogleApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleApiInterface::class.java)
    }
}