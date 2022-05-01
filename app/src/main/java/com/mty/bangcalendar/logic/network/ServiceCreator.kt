package com.mty.bangcalendar.logic.network

import retrofit2.Retrofit

object ServiceCreator {

    //临时使用提供图片的URL
    private const val PICTURE_URL = "https://www.mxmnb.cn/bangcalendar/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(PICTURE_URL)
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

}