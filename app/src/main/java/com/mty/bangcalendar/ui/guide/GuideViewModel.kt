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
            if (it.isFirstStart) {
                //如果是初次启动则初始化数据库，并更新初始化进度
                launch {
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
            } else if (BangCalendarApplication.systemDate.getDayOfWeak() == 2
                && it.lastRefreshDay != BangCalendarApplication.systemDate.day) {
                //每周一自动更新数据库
                DatabaseUpdater.updateDatabase()
            }
        }
    }

    //app首次启动初始化相关UI状态
    private val _appInitUiState by lazy { MutableStateFlow(AppInitUiState()) }
    val appInitUiState : StateFlow<AppInitUiState>
        get() = _appInitUiState

}