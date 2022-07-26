package com.mty.bangcalendar

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.mty.bangcalendar.util.CalendarUtil

class BangCalendarApplication : Application() {

    companion object {
        //提供全局Context
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        val systemDate = CalendarUtil() //系统时间
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}