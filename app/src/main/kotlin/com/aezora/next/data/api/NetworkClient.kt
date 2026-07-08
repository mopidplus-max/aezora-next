package com.aezora.next.data.api

import com.aezora.next.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val soundCloudApi: SoundCloudApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.soundcloud.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoundCloudApi::class.java)
    }

    val soundCloudApiV2: SoundCloudApiV2 by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-v2.soundcloud.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoundCloudApiV2::class.java)
    }

    val vkApi: VKApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.vk.com/method/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VKApi::class.java)
    }

    val yandexApi: YandexMusicApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.music.yandex.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YandexMusicApi::class.java)
    }
}
