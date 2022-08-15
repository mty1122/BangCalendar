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
import com.mty.bangcalendar.util.LogUtil

class GuideViewModel : ViewModel() {

    //载入GuideInitData，包括判断是否初次启动和获取主题
    private val initDataLiveData = MutableLiveData<Any?>()
    val initData: LiveData<GuideInitData> = Transformations.switchMap(initDataLiveData) {
        Repository.getGuideInitData()
    }
    fun getInitData() {
        initDataLiveData.value = initDataLiveData.value
    }

    //传递更新进度
    val refreshDataProgress: LiveData<Int>
        get() = _refreshDataProgress

    private val _refreshDataProgress = MutableLiveData<Int>()

    fun refreshDataProgress(progress: Int) {
        _refreshDataProgress.value = progress
        LogUtil.d("Guide", "init_progress = $progress")
    }

    //刷新更新细节(Compose专用)
    val refreshDetails: LiveData<String>
        get() = _refreshDetails

    private val _refreshDetails = MutableLiveData<String>()

    fun refreshDetails(details: String) {
        _refreshDetails.value = details
        LogUtil.d("Guide", "init_progress = $details")
    }

    val launchButtonEnabled: LiveData<Boolean>
        get() = _launchButtonEnabled

    private val _launchButtonEnabled = MutableLiveData<Boolean>()

    fun setLaunchButtonEnabled(isEnabled: Boolean) {
        _launchButtonEnabled.value = isEnabled
    }

    init {
        _refreshDataProgress.value = 0
    }

}