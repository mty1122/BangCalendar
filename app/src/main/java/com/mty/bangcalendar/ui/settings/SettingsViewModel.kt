package com.mty.bangcalendar.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.UpdateResponse
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService
import com.mty.bangcalendar.util.LogUtil
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    var isActivityRecreated = true

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

    //登录
    private val loginLiveData = MutableLiveData<LoginRequest>()
    val loginResponse = Transformations.switchMap(loginLiveData) {
        Repository.login(it)
    }
    fun login(request: LoginRequest) {
        loginLiveData.value = request
        LogUtil.d("Repository", "login request ${request.phone}")
    }
    fun loginFinished() {
        loginLiveData.value = LoginRequest("1", "1")
    }

    private val phoneNumLiveData = MutableLiveData<String?>()
    val phoneNum = Transformations.switchMap(phoneNumLiveData) {
        if (it != null)
            Repository.setPhoneNum(it)
        else
            Repository.getPhoneNum()
    }
    fun getPhoneNum() {
        phoneNumLiveData.value = null
    }
    fun setPhoneNum(phone: String) {
        phoneNumLiveData.value = phone
    }

    //同步偏好
    private val downloadPreferenceLiveData = MutableLiveData<LoginRequest>()
    val downloadPreference = Transformations.switchMap(downloadPreferenceLiveData) {
        Repository.downloadUserPreference(it)
    }
    fun downloadUserPreference(loginRequest: LoginRequest) {
        downloadPreferenceLiveData.value = loginRequest
        isActivityRecreated = false
    }

    private val uploadPreferenceLiveData = MutableLiveData<UserPreference>()
    val uploadResponse = Transformations.switchMap(uploadPreferenceLiveData) {
        Repository.uploadUserPreference(it)
    }
    fun uploadUserPreference(userPreference: UserPreference) {
        uploadPreferenceLiveData.value = userPreference
    }

    private val getPreferenceLiveData = MutableLiveData<String>()
    val userPreference = Transformations.switchMap(getPreferenceLiveData) {
        Repository.getUserPreference()
    }
    fun getUserPreference(phone: String) {
        getPreferenceLiveData.value = phone
    }

    private val setPreferenceLiveData = MutableLiveData<UserPreference>()
    val setResponse = Transformations.switchMap(setPreferenceLiveData) {
        Repository.setUserPreference(it)
    }
    fun setUserPreference(userPreference: UserPreference) {
        setPreferenceLiveData.value = userPreference
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
        intentFilter.addAction("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        BangCalendarApplication.context.registerReceiver(refreshDataResultReceiver, intentFilter)
    }

    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshDataResultReceiver)
    }

}