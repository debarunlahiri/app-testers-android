package com.summitcodeworks.apptesters.apiClient

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.summitcodeworks.apptesters.apiInterface.ApiInterface
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val PROD_BASE_URL = "https://apptesters-backend.onrender.com"
    private const val DEV_BASE_URL = "http://192.168.0.36:8082/"

    enum class Environment {
        DEV, PROD
    }

    // Set this to switch between environments
    var currentEnvironment = Environment.PROD // Default to DEV
    var API_KEY = "" // Replace this with your actual API key

    public val baseUrl: String
        get() = when (currentEnvironment) {
            Environment.DEV -> DEV_BASE_URL
            Environment.PROD -> PROD_BASE_URL
        }

    fun initialize(apiKey: String, environment: Environment) {
        this.API_KEY = apiKey
        this.currentEnvironment = environment
    }

    private fun getRetrofitInstance(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
//            .addInterceptor(ChuckerInterceptor(context))
            .addInterceptor { chain ->
                val originalRequest: Request = chain.request()
                val newRequest: Request = originalRequest.newBuilder()
                    .header("api-key", API_KEY) // Add API key header
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl) // Use the selected base URL
            .client(client) // Set the client with the interceptors
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiInterface: (Context) -> ApiInterface = { context: Context ->
        getRetrofitInstance(context).create(ApiInterface::class.java)
    }
}
