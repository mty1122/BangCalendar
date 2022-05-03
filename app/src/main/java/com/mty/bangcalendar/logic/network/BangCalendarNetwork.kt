package com.mty.bangcalendar.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BangCalendarNetwork {

    private val eventPictureService = ServiceCreator.create<EventPictureService>()

    private val databaseRefreshService = ServiceCreator.create<DatabaseRefreshService>()

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

    suspend fun getEventPicture(eventId: String) =
        eventPictureService.getEventPicture(eventId).await()

    fun getCharacterList() = databaseRefreshService.getCharacterList()

    fun getEventList() = databaseRefreshService.getEventList()

}