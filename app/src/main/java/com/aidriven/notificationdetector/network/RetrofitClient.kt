package com.aidriven.notificationdetector.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Use 10.0.2.2 for Android emulator to access localhost
    // Use your computer's IP address for physical device (e.g., "http://192.168.1.100:5001/")
    private const val BASE_URL = "http://10.55.205.104:5001/"
    
    // Logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttp client with timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Lazy initialization of Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Lazy initialization of ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    // Function to get base URL (useful for configuration)
    fun getBaseUrl(): String = BASE_URL
    
    // Function to update base URL if needed (for physical device)
    fun createCustomApiService(baseUrl: String): ApiService {
        val customRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return customRetrofit.create(ApiService::class.java)
    }
}
