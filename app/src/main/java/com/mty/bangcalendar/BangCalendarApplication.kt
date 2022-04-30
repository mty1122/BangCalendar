package com.mty.bangcalendar

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class BangCalendarApplication : Application() {

    companion object {
        //提供全局Context
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}