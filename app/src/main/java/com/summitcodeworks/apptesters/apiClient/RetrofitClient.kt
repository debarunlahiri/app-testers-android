package com.summitcodeworks.apptesters.apiClient

import android.annotation.SuppressLint
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.summitcodeworks.apptesters.apiInterface.ApiInterface
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

//    private const val BASE_URL = "http://192.168.0.35:6000/"
    private const val BASE_URL = "http://13.51.195.70:5000/"
    private var retrofit: Retrofit? = null

    public lateinit var mContext: Context

    // Replace this with your actual API key
    public var API_KEY = ""

    fun getRetrofitInstance(context: Context): Retrofit {
        if (retrofit == null) {
            val client = OkHttpClient.Builder()
//                .addInterceptor(ChuckerInterceptor(context)) // Use passed context
                .addInterceptor { chain ->
                    val originalRequest: Request = chain.request()
                    val newRequest: Request = originalRequest.newBuilder()
                        .header("api-key", API_KEY) // Add API key header
                        .build()
                    chain.proceed(newRequest)
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Set the client with the interceptors
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun initialize(mContext: Context) {
        this.mContext = mContext
    }

    val apiInterface: ApiInterface
        get() {
            // Ensure that getRetrofitInstance is called to initialize the Retrofit instance
            return getRetrofitInstance(mContext).create(ApiInterface::class.java)
        }
}
