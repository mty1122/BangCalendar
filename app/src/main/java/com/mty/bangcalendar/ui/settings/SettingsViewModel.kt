package com.mty.bangcalendar.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.DatabaseUpdater
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.logic.repository.SettingsRepository
import com.mty.bangcalendar.ui.settings.state.SettingsUiState
import com.mty.bangcalendar.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    //设置界面UiState
    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState = _settingsUiState.asStateFlow()

    fun setPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            settingsRepository.setPhoneNumber(phoneNumber)
            _settingsUiState.update {
                it.copy(phoneNumber = phoneNumber)
            }
        }
    }

    init {
        //启动时后台自动获取UiState
        viewModelScope.launch {
            //先获取手机号码，及时更新登录状态
            val phoneNumber = settingsRepository.getPhoneNumber()
            _settingsUiState.update {
                it.copy(phoneNumber = phoneNumber)
            }
            //获取新版本信息
            val appUpdateInfo = settingsRepository.getAppUpdateInfo().getOrNull()
            val currentVersionCode = settingsRepository.getVersionCode()
            val lastRefreshDate = settingsRepository.getLastRefreshDate()
            _settingsUiState.update {
                it.copy(
                    lastRefreshDate = lastRefreshDate,
                    hasNewVersion = currentVersionCode < (appUpdateInfo?.versionCode
                        ?: currentVersionCode),
                    newVersionName = appUpdateInfo?.versionName ?: ""
                )
            }
        }
    }

    //更新数据库
    fun refreshDataBase() = DatabaseUpdater.updateDatabase()

    //发送验证码
    suspend fun sendSms(phoneNumber: String) = settingsRepository.sendSms(phoneNumber)

    //登录
    suspend fun login(phoneNumber: String, smsCode: String) =
        settingsRepository.login(phoneNumber, smsCode)

    //同步偏好
    fun backupPreference() {
        viewModelScope.launch {
            val result = settingsRepository.backupPreference()
            if (result)
                toast("备份成功")
            else
                toast("备份失败，请重试，或检查网络连接")
        }
    }
    fun recoveryPreference(onRecoverySuccess: (UserPreference) -> Unit) {
        viewModelScope.launch {
            val preference = settingsRepository.recoveryPreference()
            if (preference != null) {
                onRecoverySuccess(preference)
            } else {
                toast("恢复失败，请重试，或检查网络连接")
            }
        }
    }

}