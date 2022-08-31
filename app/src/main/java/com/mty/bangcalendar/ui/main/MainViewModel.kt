package com.mty.bangcalendar.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.logic.model.IntDate
import kotlinx.coroutines.launch

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
                setJumpDate(IntDate(startDate))
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
    val jumpDate: LiveData<IntDate>
        get() = _jumpDate

    private val _jumpDate = MutableLiveData<IntDate>()

    fun setJumpDate(startDate: IntDate) {
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

    private val _todayEvent = MutableLiveData<Event?>()
    val todayEvent: LiveData<Event?>
        get() = _todayEvent
    fun getTodayEvent() {
        viewModelScope.launch {
            _todayEvent.value = Repository.getEventByDate(systemDate.toDate())
        }
    }

    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?>
        get() = _event
    fun getEventByDate(date: IntDate) {
        viewModelScope.launch {
            _event.value = Repository.getEventByDate(date)
        }
    }

    private val _characterInMonth = MutableLiveData<List<Character>>()
    val characterInMonth: LiveData<List<Character>>
        get() = _characterInMonth
    fun getCharacterByMonth(month: Int) {
        viewModelScope.launch {
            _characterInMonth.value = Repository.getCharacterByMonth(month)
        }
    }

    //dailyTag服务
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName
    fun getUserName() {
        viewModelScope.launch {
           _userName.value = Repository.getUserName()
        }
    }

    private val _preferenceBand = MutableLiveData<String>()
    val preferenceBand: LiveData<String>
        get() = _preferenceBand
    fun getPreferenceBand() {
        viewModelScope.launch {
            _preferenceBand.value = Repository.getPreferenceBand()
        }
    }

    private val _preferenceNearlyBandEvent = MutableLiveData<Event?>()
    val preferenceNearlyBandEvent: LiveData<Event?>
        get() = _preferenceNearlyBandEvent
    fun getPreferenceNearlyBandEvent(character1Id: Int) {
        viewModelScope.launch {
            _preferenceNearlyBandEvent.value =
                Repository.getBandEventByDate(systemDate.toDate(), character1Id)
        }
    }

    private val _preferenceCharacterId = MutableLiveData<Int>()
    val preferenceCharacterId: LiveData<Int>
        get() = _preferenceCharacterId
    fun getPreferenceCharacterId() {
        viewModelScope.launch {
            _preferenceCharacterId.value =  Repository.getPreferenceCharacter()
        }
    }

    private val _preferenceCharacter = MutableLiveData<Character>()
    val preferenceCharacter: LiveData<Character>
        get() = _preferenceCharacter
    fun getPreferenceCharacter(id: Int) {
        viewModelScope.launch {
            _preferenceCharacter.value = Repository.getCharacterById(id)
        }
    }

    //附加提醒
    private val _additionalTip = MutableLiveData<String>()
    val additionalTip: LiveData<String>
        get() = _additionalTip
    fun getAdditionalTip() {
        viewModelScope.launch {
            _additionalTip.value = Repository.getAdditionalTip()
        }
    }
    fun setAdditionalTip(additionalTip: String) {
        viewModelScope.launch {
            _additionalTip.value = Repository.setAdditionalTip(additionalTip)
        }
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