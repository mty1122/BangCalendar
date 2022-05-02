package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.util.LogUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class RefreshEventService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        //暂时无需与activity绑定，待完善
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        thread {
            val isInit = intent.getBooleanExtra("isInit", true)
            val gson = Gson()
            val typeOf = object : TypeToken<List<Event>>() {}.type
            if (isInit) {
                val eventReader = BufferedReader(
                    InputStreamReader(
                    Repository.getEventJSONStreamFromAssets())
                )
                val eventList = gson.fromJson<List<Event>>(eventReader, typeOf)
                for (event in eventList) {
                    Repository.addEventToDatabase(event)
                    LogUtil.d("AppInit", "向数据库加入活动：$event")
                }
            }
            sendMessage()
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendMessage() {
        val intent = Intent("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

}