package com.mty.bangcalendar.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.DatabaseUpdater
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.GetPreferenceRequest
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.SmsRequest
import com.mty.bangcalendar.logic.model.UpdateResponse
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.util.SecurityUtil
import com.mty.bangcalendar.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel : ViewModel() {

    //更新数据库
    fun refreshDataBase() = DatabaseUpdater.updateDatabase()

    //发送验证码
    suspend fun sendSms(request: SmsRequest) = Repository.sendSms(request)

    //登录
    suspend fun login(request: LoginRequest) = Repository.login(request)

    //LiveData线程不安全，同步数据需要在子线程中进行，因此选择线程安全的StateFlow
    private val phoneNumFlow = MutableStateFlow("")
    val phoneNum: StateFlow<String>
        get() = phoneNumFlow
    fun getPhoneNum() {
        viewModelScope.launch {
            phoneNumFlow.value = Repository.getPhoneNum()
        }
    }
    fun setPhoneNum(phone: String) {
        viewModelScope.launch {
            Repository.setPhoneNum(phone)
            phoneNumFlow.value = phone
        }
    }

    //同步偏好
    fun backupPreference() {
        viewModelScope.launch(Dispatchers.IO) {
            val preference = Repository.getUserPreference()
            preference.encrypt() //加密后上传
            val result = Repository.uploadUserPreference(preference)
            val response = result.getOrNull()
            withContext(Dispatchers.Main) {
                if (response != null && response.string() == "OK")
                    toast("备份成功")
                else
                    toast("备份失败，请重试，或检查网络连接")
            }
        }
    }
    fun recoveryPreference(onRecoverySuccess: (UserPreference) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = Repository.downloadUserPreference(
                GetPreferenceRequest(phoneNum.value,
                    SecurityUtil.encrypt(SecurityUtil.aesKey, phoneNum.value))
            )
            val preference = result.getOrNull()
            if (preference != null) {
                preference.decrypt() //解密后入库
                Repository.setUserPreference(preference)
                withContext(Dispatchers.Main) {
                    onRecoverySuccess(preference)
                }
            } else {
                withContext(Dispatchers.Main) {
                    toast("恢复失败，请重试，或检查网络连接")
                }
            }
        }
    }

    private val _appUpdateInfo = MutableLiveData<Result<UpdateResponse>>()
    val appUpdateInfo: LiveData<Result<UpdateResponse>>
        get() = _appUpdateInfo
    fun getAppUpdateInfo() {
        viewModelScope.launch {
            _appUpdateInfo.value = Repository.getAppUpdateInfo()
        }
    }

}