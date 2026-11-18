package com.example.tiketiin.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.tiketiin.data.remote.api.SupabaseApiService

object RetrofitClient {

    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()

            .addInterceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .header(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                    )

                accessToken?.let { token ->
                    request.header(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(request.build())
            }

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val api: SupabaseApiService by lazy {
        retrofit.create(SupabaseApiService::class.java)
    }
}