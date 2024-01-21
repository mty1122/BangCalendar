package com.mty.bangcalendar

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mty.bangcalendar.util.CalendarUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BangCalendarApplication : Application() {

    companion object {
        //提供全局Context
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        val systemDate = CalendarUtil() //系统时间
        var isNavigationBarImmersionEnabled = false //记录小白条沉浸偏好
    }

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_DATE_CHANGED) {
                systemDate.refreshCalendar()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        //监听系统日期变化
        val intentFilter = IntentFilter(Intent.ACTION_DATE_CHANGED)
        registerReceiver(dateChangeReceiver, intentFilter)
    }

    override fun onTerminate() {
        unregisterReceiver(dateChangeReceiver)
        super.onTerminate()
    }

}