package com.mty.bangcalendar.ui.main

import android.content.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.enum.IntentActions
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.util.CalendarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置
    var birthdayAway: Int? = null //关注角色生日还有多少天

    //活动起始/终止时间
    var eventStartTime: Long? = null
    var eventEndTime: Long? = null

    var isActivityFirstStart = true
    var isActivityRecreated = true

    //应用设置更改
    private val onSettingsChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "signature" -> getUserName()
            "band" -> getPreferenceBand()
            "character" -> getPreferenceCharacter()
            "theme" ->recreateActivity()
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

        Repository.registerOnDefaultPreferenceChangeListener(onSettingsChangeListener)

        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentActions.JUMP_DATE_ACTION.value)
        ContextCompat.registerReceiver(BangCalendarApplication.context, jumpDateReceiver,
            intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
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

    private val _preferenceCharacter = MutableLiveData<Character>()
    val preferenceCharacter: LiveData<Character>
        get() = _preferenceCharacter
    fun getPreferenceCharacter() {
        viewModelScope.launch {
            val characterId = Repository.getPreferenceCharacter()
            _preferenceCharacter.value = Repository.getCharacterById(characterId)
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

    //更改主题，这里采用flow，因为需要activity在不可见时就进行重启
    private val _activityRecreate = MutableStateFlow(0)
    val activityRecreate: StateFlow<Int>
        get() = _activityRecreate
    private fun recreateActivity() {
        isActivityRecreated = false
        _activityRecreate.value++
    }

    //取消注册Broadcast
    override fun onCleared() {
        super.onCleared()
        Repository.unregisterOnDefaultPreferenceChangeListener(onSettingsChangeListener)
        BangCalendarApplication.context.unregisterReceiver(jumpDateReceiver)
    }

}