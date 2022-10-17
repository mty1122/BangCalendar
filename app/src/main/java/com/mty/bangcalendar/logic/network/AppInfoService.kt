package com.mty.bangcalendar.logic.network

import com.mty.bangcalendar.logic.model.UpdateResponse
import retrofit2.Call
import retrofit2.http.GET

interface AppInfoService {

    @GET("app_service/update.php")
    fun getUpdateInfo(): Call<UpdateResponse>

}