package com.mty.bangcalendar.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService

class SettingsViewModel : ViewModel() {

    //接收service发来的更新进度
    private val refreshDataResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val result = intent.getIntExtra("result", 0)
            _refreshDataResult.value = result
        }
    }

    //传递更新进度
    val refreshDataResult: LiveData<Int>
        get() = _refreshDataResult

    private val _refreshDataResult = MutableLiveData<Int>()

    //更新数据库
    fun refreshDataBase(context: Context) {
        refreshCharacter(context)
        refreshEvent(context)
    }

    //更新数据库数据
    private fun refreshCharacter(context: Context) {
        val intent = Intent(context, CharacterRefreshService::class.java)
        intent.putExtra("isInit", false)
        context.startService(intent)
    }

    private fun refreshEvent(context: Context) {
        val intent = Intent(context, EventRefreshService::class.java)
        intent.putExtra("isInit", false)
        context.startService(intent)
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        BangCalendarApplication.context.registerReceiver(refreshDataResultReceiver, intentFilter)
    }

    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshDataResultReceiver)
    }

}