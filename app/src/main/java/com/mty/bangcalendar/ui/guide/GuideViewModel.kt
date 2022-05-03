package com.mty.bangcalendar.ui.guide

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService

class GuideViewModel : ViewModel() {

    private val isFirstStartLiveData = MutableLiveData<Any?>()

    //判断是否首次启动
    val isFirstStart: LiveData<Boolean> = Transformations.switchMap(isFirstStartLiveData) {
        Repository.isFirstStart()
    }

    //调用该方法使isFirstStartLiveData的值改变从而调用Repository.isFirstStart()方法
    fun isFirstStart() {
        isFirstStartLiveData.value = isFirstStartLiveData.value
    }

    //接收service发来的更新进度
    private val refreshDataProgressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            _refreshDataProgress.value = _refreshDataProgress.value?.plus(50)
        }
    }

    //传递更新进度
    val refreshDataProgress: LiveData<Int>
        get() = _refreshDataProgress

    private val _refreshDataProgress = MutableLiveData<Int>()

    //初始化（更新）数据库
    fun initDataBase(context: Context) {
        addCharacter(context)
        addEvent(context)
    }

    //初始化应用
    private fun addCharacter(context: Context) {
        val intent = Intent(context, CharacterRefreshService::class.java)
        intent.putExtra("isInit", true)
        context.startService(intent)
    }

    private fun addEvent(context: Context) {
        val intent = Intent(context, EventRefreshService::class.java)
        intent.putExtra("isInit", true)
        context.startService(intent)
    }

    init {
        _refreshDataProgress.value = 0
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        BangCalendarApplication.context.registerReceiver(refreshDataProgressReceiver, intentFilter)
    }

    //取消注册Broadcast
    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshDataProgressReceiver)
    }

}