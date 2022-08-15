package com.mty.bangcalendar.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.CalendarUtil

class MainViewModel : ViewModel() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置
    var birthdayAway: Int? = null //关注角色生日还有多少天

    //活动起始/终止时间
    var eventStartTime: Long? = null
    var eventEndTime: Long? = null

    //应用设置更改
    private val refreshSettingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val settingsCategory = intent
                .getIntExtra("settingsCategory", SettingsActivity.REFRESH_USERNAME)
            when (settingsCategory) {
                SettingsActivity.REFRESH_USERNAME -> getUserName()
                SettingsActivity.REFRESH_BAND -> getPreferenceBand()
                SettingsActivity.REFRESH_CHARACTER -> getPreferenceCharacterId()
            }
        }
    }

    //跳转日期
    private val jumpDateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val startDate = intent.getIntExtra("current_start_date", -1)
            if (startDate != -1)
                setJumpDate(startDate)
        }
    }

    //当前选中的日期
    val currentDate: LiveData<CalendarUtil>
    get() = _currentDate

    private val _currentDate = MutableLiveData<CalendarUtil>()

    fun refreshCurrentDate() {
        _currentDate.value = _currentDate.value
    }

    //当前选中item
    val selectedItem: LiveData<Int>
    get() = _selectedItem

    private val _selectedItem = MutableLiveData<Int>()

    fun setSelectedItem(item: Int) {
        _selectedItem.value = item
    }

    //生日卡片
    val birthdayCard: LiveData<Int>
    get() = _birthdayCard

    private val _birthdayCard = MutableLiveData<Int>()

    fun refreshBirthdayCard(id: Int) {
        _birthdayCard.value = id
    }

    //跳转日期
    val jumpDate: LiveData<Int>
        get() = _jumpDate

    private val _jumpDate = MutableLiveData<Int>()

    fun setJumpDate(startDate: Int) {
        _jumpDate.value = startDate
    }

    init {
        _currentDate.value = CalendarUtil()
        _selectedItem.value = systemDate.day

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.mty.bangcalendar.SETTINGS_CHANGE")
        BangCalendarApplication.context.registerReceiver(refreshSettingsReceiver, intentFilter)

        val intentFilter2 = IntentFilter()
        intentFilter2.addAction("com.mty.bangcalendar.JUMP_DATE")
        BangCalendarApplication.context.registerReceiver(jumpDateReceiver, intentFilter2)
    }

    private val todayEventLiveData = MutableLiveData<Any>()
    val todayEvent: LiveData<Event?> = Transformations.switchMap(todayEventLiveData) {
        Repository.getEventByDate(systemDate.getDate())
    }
    fun getTodayEvent() {
        todayEventLiveData.value = todayEventLiveData.value
    }

    private val searchDateLiveData = MutableLiveData<Int>()
    val event: LiveData<Event?> = Transformations.switchMap(searchDateLiveData) {
        Repository.getEventByDate(it)
    }
    fun getEventByDate(date: Int) {
        searchDateLiveData.value = date
    }

    private val getCharacterByMonthLiveData = MutableLiveData<Int>()
    val characterInMonth = Transformations.switchMap(getCharacterByMonthLiveData) {
        Repository.getCharacterByMonth(it)
    }
    fun getCharacterByMonth(month: Int) {
        getCharacterByMonthLiveData.value = month
    }

    //dailyTag服务
    private val userNameLiveData = MutableLiveData<String>()
    val userName = Transformations.switchMap(userNameLiveData) {
        Repository.getUserName()
    }
    fun getUserName() {
        userNameLiveData.value = userNameLiveData.value
    }

    private val preferenceBandLiveData = MutableLiveData<String>()
    val preferenceBand = Transformations.switchMap(preferenceBandLiveData) {
        Repository.getPreferenceBand()
    }
    fun getPreferenceBand() {
        preferenceBandLiveData.value = preferenceBandLiveData.value
    }

    private val preferenceNearlyBandEventLiveData = MutableLiveData<Int>()
    val preferenceNearlyBandEvent = Transformations.switchMap(preferenceNearlyBandEventLiveData) {
        Repository.getBandEventByDate(systemDate.getDate(), it)
    }
    fun getPreferenceNearlyBandEvent(character1Id: Int) {
        preferenceNearlyBandEventLiveData.value = character1Id
    }

    private val preferenceCharacterIdLiveData = MutableLiveData<Int>()
    val preferenceCharacterId = Transformations.switchMap(preferenceCharacterIdLiveData) {
        Repository.getPreferenceCharacter()
    }
    fun getPreferenceCharacterId() {
        preferenceCharacterIdLiveData.value = preferenceCharacterIdLiveData.value
    }

    private val preferenceCharacterLiveData = MutableLiveData<Int>()
    val preferenceCharacter = Transformations.switchMap(preferenceCharacterLiveData) {
        Repository.getCharacterById(it)
    }
    fun getPreferenceCharacter(id: Int) {
        preferenceCharacterLiveData.value = id
    }

    //附加提醒
    private val additionalTipLiveData = MutableLiveData<String?>()
    val additionalTip = Transformations.switchMap(additionalTipLiveData) {
        if (it != null)
            Repository.setAdditionalTip(it)
        else
            Repository.getAdditionalTip()
    }
    fun getAdditionalTip() {
        additionalTipLiveData.value = null
    }
    fun setAdditionalTip(additionalTip: String) {
        additionalTipLiveData.value = additionalTip
    }

    suspend fun getEventPic(eventId: String, onPicReady: (Drawable) -> Unit) {
        Repository.getEventPic(eventId)?.let {
            onPicReady(it)
        }
    }

    //取消注册Broadcast
    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshSettingsReceiver)
        BangCalendarApplication.context.unregisterReceiver(jumpDateReceiver)
    }

}