package com.mty.bangcalendar.logic.network

import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import retrofit2.Call
import retrofit2.http.GET

interface DatabaseRefreshService {

    @GET("data/Character.json")
    fun getCharacterList(): Call<List<Character>>

    @GET("data/Event.json")
    fun getEventList(): Call<List<Event>>

}