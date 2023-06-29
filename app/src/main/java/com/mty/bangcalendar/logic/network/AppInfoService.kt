package com.mty.bangcalendar.logic.network

import com.mty.bangcalendar.logic.model.UpdateResponse
import retrofit2.Call
import retrofit2.http.GET

interface AppInfoService {

    @GET("index.php/update")
    fun getUpdateInfo(): Call<UpdateResponse>

}