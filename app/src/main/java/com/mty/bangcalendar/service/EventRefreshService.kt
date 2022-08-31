package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.LogUtil
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class EventRefreshService : Service() {

    companion object {

        fun sendMessage(result: Int) {
            val context = BangCalendarApplication.context
            val intent = Intent("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
            intent.setPackage(context.packageName)
            intent.putExtra("result", result)
            context.sendBroadcast(intent)
        }

    }

    private val refreshBinder = RefreshBinder()

    class RefreshBinder : Binder() {

        @OptIn(ExperimentalSerializationApi::class)
        fun refresh(onItemAddFinished: (Int, String) -> Unit) {
            val eventReader = Repository.getEventJSONStreamFromAssets()
            val eventList = Json.decodeFromStream<List<Event>>(eventReader)
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                for (event in eventList) {
                    withContext(Dispatchers.IO) {
                        Repository.addEventToDatabase(event)
                    }
                    val progress = event.id.toInt() * 50 / eventList.size
                    val details = "载入活动：${event.id}"
                    onItemAddFinished(progress, details)
                }
                coroutineScope.cancel()
            }
        }

    }

    override fun onBind(intent: Intent): IBinder = refreshBinder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Repository.getEventListFromInternet().enqueue(object :
            Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>,
                                    response: Response<List<Event>>) {
                val eventList = response.body()
                if (eventList != null) {
                    addEventToDatabase(eventList)
                } else {
                    sendMessage(SettingsActivity.REFRESH_EVENT_FAILURE)
                    stopSelf()
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                t.printStackTrace()
                sendMessage(SettingsActivity.REFRESH_EVENT_FAILURE)
                stopSelf()
            }
        })
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addEventToDatabase(eventList: List<Event>) {
        thread {
            for (event in eventList) {
                Repository.addEventToDatabase(event)
                LogUtil.d("Database", "向数据库加入活动：$event")
            }
            sendMessage(SettingsActivity.REFRESH_EVENT_SUCCESS)
            Repository.setLastRefreshDay(BangCalendarApplication.systemDate.day)
            stopSelf()
        }
    }

}