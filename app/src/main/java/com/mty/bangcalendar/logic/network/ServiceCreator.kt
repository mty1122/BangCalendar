package com.mty.bangcalendar.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {

    //临时使用提供图片的URL
    private const val TEMP_URL = "https://www.mxmnb.cn/bangcalendar/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(TEMP_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

}