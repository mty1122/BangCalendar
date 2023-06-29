package com.mty.bangcalendar.logic.network

import com.mty.bangcalendar.logic.model.GetPreferenceRequest
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.SmsRequest
import com.mty.bangcalendar.logic.model.UserPreference
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {

    @POST("index.php/sms")
    fun sendSms(@Body request: SmsRequest): Call<ResponseBody>

    @POST("index.php/login")
    fun login(@Body request: LoginRequest): Call<ResponseBody>

    @POST("index.php/set")
    fun setPreference(@Body userPreference: UserPreference): Call<ResponseBody>

    @POST("index.php/get")
    fun getPreference(@Body request: GetPreferenceRequest): Call<UserPreference>

}