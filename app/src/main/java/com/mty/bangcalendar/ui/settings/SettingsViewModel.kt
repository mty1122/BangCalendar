package com.mty.bangcalendar.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.enum.IntentActions
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.*
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService
import com.mty.bangcalendar.util.SecurityUtil
import com.mty.bangcalendar.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        context.startService(intent)
    }

    private fun refreshEvent(context: Context) {
        val intent = Intent(context, EventRefreshService::class.java)
        context.startService(intent)
    }

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

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentActions.REFRESH_DATABASE_FINISH_ACTION.value)
        ContextCompat.registerReceiver(BangCalendarApplication.context, refreshDataResultReceiver,
            intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshDataResultReceiver)
    }

}