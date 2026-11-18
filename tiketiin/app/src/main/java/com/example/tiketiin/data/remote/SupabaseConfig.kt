package com.example.tiketiin.data.remote

import com.example.tiketiin.BuildConfig

object SupabaseConfig {

    val BASE_URL: String
        get() = BuildConfig.SUPABASE_URL
            .trimEnd('/') + "/"

    val PUBLISHABLE_KEY: String
        get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY

}