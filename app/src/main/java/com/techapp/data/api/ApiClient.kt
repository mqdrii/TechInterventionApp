package com.techapp.data.api

import android.content.Context
import com.techapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var currentBaseUrl: String = ""
    private var cachedService: TechApiService? = null

    /** URL del server cloud — gli utenti non devono configurare nulla */
    const val DEFAULT_SERVER_URL = "https://techintervention-api.onrender.com/"

    fun getService(context: Context): TechApiService {
        val sessionManager = SessionManager(context)
        var serverUrl = sessionManager.getServerUrl().trim()
        if (serverUrl.isBlank()) {
            serverUrl = DEFAULT_SERVER_URL
        }
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/"
        }

        if (serverUrl != currentBaseUrl || cachedService == null) {
            currentBaseUrl = serverUrl

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val token = sessionManager.getAuthToken()
                    val requestBuilder = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")

                    if (!token.isNullOrBlank()) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }

                    chain.proceed(requestBuilder.build())
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            cachedService = retrofit.create(TechApiService::class.java)
        }

        return cachedService!!
    }

    fun reset() {
        cachedService = null
        currentBaseUrl = ""
    }
}
