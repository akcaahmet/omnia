package com.ahmetakca.omnia

import android.content.Context
import com.ahmetakca.omnia.adapter.LocalDateTimeAdapter
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.service.TokenAuthenticator
import com.ahmetakca.omnia.util.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

object NetworkClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    fun createApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val okHttpClient = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(tokenManager))
            .build()

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build().create(ApiService::class.java)
    }
}