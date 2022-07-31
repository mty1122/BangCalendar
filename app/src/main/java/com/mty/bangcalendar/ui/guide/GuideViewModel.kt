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
import com.mty.bangcalendar.logic.model.GuideInitData

class GuideViewModel : ViewModel() {

    //载入GuideInitData，包括判断是否初次启动和获取主题
    private val initDataLiveData = MutableLiveData<Any?>()
    val initData: LiveData<GuideInitData> = Transformations.switchMap(initDataLiveData) {
        Repository.getGuideInitData()
    }
    fun getInitData() {
        initDataLiveData.value = initDataLiveData.value
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