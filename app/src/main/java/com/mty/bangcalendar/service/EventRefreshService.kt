package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class EventRefreshService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        //暂时无需与activity绑定，待完善
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            val isInit = intent.getBooleanExtra("isInit", true)
            if (isInit) {
                val gson = Gson()
                val typeOf = object : TypeToken<List<Event>>() {}.type
                val eventReader = BufferedReader(
                    InputStreamReader(
                    Repository.getEventJSONStreamFromAssets())
                )
                val eventList = gson.fromJson<List<Event>>(eventReader, typeOf)
                addEventToDatabase(eventList)
            } else {
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
            }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendMessage(result: Int) {
        val intent = Intent("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        intent.setPackage(packageName)
        intent.putExtra("result", result)
        sendBroadcast(intent)
    }

    private fun addEventToDatabase(eventList: List<Event>) {
        thread {
            for (event in eventList) {
                Repository.addEventToDatabase(event)
                LogUtil.d("Database", "向数据库加入活动：$event")
            }
            sendMessage(SettingsActivity.REFRESH_EVENT_SUCCESS)
            stopSelf()
        }
    }

}