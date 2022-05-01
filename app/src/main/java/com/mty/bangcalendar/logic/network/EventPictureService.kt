package com.mty.bangcalendar.logic.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EventPictureService {

    @GET("event/banner_memorial_event{eventId}.png")
    fun getEventPicture(@Path("eventId")eventId: String): Call<ResponseBody>

}