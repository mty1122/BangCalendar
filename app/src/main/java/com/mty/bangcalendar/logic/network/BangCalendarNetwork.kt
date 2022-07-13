package com.mty.bangcalendar.logic.network

import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.UserPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BangCalendarNetwork {

    private val databaseRefreshService = ServiceCreator.create<DatabaseRefreshService>()

    private val userService = ServiceCreator.create<UserService>()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine {
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) it.resume(body)
                    else it.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    it.resumeWithException(t)
                }
            })
        }
    }

    suspend fun login(request: LoginRequest) = userService.login(request).await()

    suspend fun setUserPreference(userPreference: UserPreference) =
        userService.setPreference(userPreference).await()

    suspend fun getUserPreference(request: LoginRequest) =
        userService.getPreference(request).await()

    fun getCharacterList() = databaseRefreshService.getCharacterList()

    fun getEventList() = databaseRefreshService.getEventList()

}