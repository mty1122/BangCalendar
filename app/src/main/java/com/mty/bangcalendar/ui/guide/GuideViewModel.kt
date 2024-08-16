package com.mty.bangcalendar.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.DatabaseUpdater
import com.mty.bangcalendar.logic.repository.GuideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val guideRepository: GuideRepository
) : ViewModel() {

    //载入GuideInitData，包括判断是否初次启动和获取全局偏好
    val initData = viewModelScope.async {
        guideRepository.fetchGuideInitData().also {
            //also后的内容主要执行后台任务
            if (it.isFirstStart) {
                //执行首次启动初始化相关业务逻辑
                firstStartInit()
            }else if (BangCalendarApplication.systemDate.getDayOfWeak() == 2
                && BangCalendarApplication.systemDate.toDate().value == it.lastRefreshDate.value) {
                //每周一自动更新数据库
                DatabaseUpdater.updateDatabase()
            }
        }
    }

    //为了避免本函数被内联，导致阻塞调用方协程（async.await()），因此不能为private
    internal fun firstStartInit() {
        viewModelScope.launch {
            guideRepository.setDefaultPreference()
            DatabaseUpdater.initDatabase { progress, detail ->
                withContext(Dispatchers.Main) {
                    _appInitUiState.value = AppInitUiState(progress, detail)
                }
            }.await()
            guideRepository.setNotFirstStart()
            //数据库初始化完成
            _appInitUiState.value = AppInitUiState(100,
                BangCalendarApplication.context.getString(R.string.init_complete))
        }
    }

    //app首次启动初始化相关UI状态
    private val _appInitUiState by lazy { MutableStateFlow(AppInitUiState()) }
    val appInitUiState : StateFlow<AppInitUiState>
        get() = _appInitUiState.asStateFlow()

}