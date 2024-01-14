package com.mty.bangcalendar.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.GuideInitData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuideViewModel : ViewModel() {

    //载入GuideInitData，包括判断是否初次启动和获取全局偏好
    fun getInitData(onDataReady: (GuideInitData) -> Unit) {
        viewModelScope.launch {
            onDataReady(Repository.getGuideInitData())
        }
    }

    //app首次启动初始化相关UI状态
    private val _appInitUiState by lazy { MutableStateFlow(AppInitUiState()) }
    val appInitUiState : StateFlow<AppInitUiState>
        get() = _appInitUiState
    fun updateProgress(progress: Int, details: String) {
        _appInitUiState.update { currentState->
            currentState.copy(
                initProgress = progress,
                initDetails = details
            )
        }
    }
    fun setLaunchButtonEnabled(enable: Boolean) {
        _appInitUiState.update { currentState->
            currentState.copy(launchButtonEnabled = enable)
        }
    }

    //app首次启动初始化相关逻辑
    suspend fun isNotFirstStart() {
        Repository.isNotFirstStart()
    }
    suspend fun setDefaultPreference() {
        Repository.setDefaultPreference()
    }

}