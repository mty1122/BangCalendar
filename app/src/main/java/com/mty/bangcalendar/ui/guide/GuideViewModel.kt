package com.mty.bangcalendar.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.GuideInitData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GuideViewModel : ViewModel() {

    //载入GuideInitData，包括判断是否初次启动和获取主题
    fun getInitData(onDataReady: (GuideInitData) -> Unit) {
        viewModelScope.launch {
            onDataReady(Repository.getGuideInitData())
        }
    }
    suspend fun isNotFirstStart() {
        Repository.isNotFirstStart()
    }

    //传递更新进度
    private val _refreshDataProgress by lazy { MutableStateFlow(0) }

    val refreshDataProgress: StateFlow<Int>
        get() = _refreshDataProgress

    fun refreshDataProgress(progress: Int) {
        _refreshDataProgress.value = progress
    }

    //刷新更新细节(Compose专用)
    private val _refreshDetails by lazy { MutableStateFlow("") }

    val refreshDetails: StateFlow<String>
        get() = _refreshDetails

    fun refreshDetails(details: String) {
        _refreshDetails.value = details
    }

    private val _launchButtonEnabled by lazy { MutableStateFlow(false) }

    val launchButtonEnabled: StateFlow<Boolean>
        get() = _launchButtonEnabled

    fun setLaunchButtonEnabled(isEnabled: Boolean) {
        _launchButtonEnabled.value = isEnabled
    }

    suspend fun setDefaultPreference() {
        Repository.setDefaultPreference()
    }

}