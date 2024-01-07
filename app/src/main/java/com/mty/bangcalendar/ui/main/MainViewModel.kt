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
import com.mty.bangcalendar.logic.model.DailyTag
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.EventUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置

    //活动起始/终止时间
    var eventStartTime: Long? = null
    var eventEndTime: Long? = null

    var isActivityFirstStart = true
    var isActivityRecreated = true

    //统计初次启动完成加载组件的数量
    private val _loadedComponentAmounts = MutableLiveData(0)
    val loadedComponentAmounts: LiveData<Int>
        get() = _loadedComponentAmounts

    fun componentLoadCompleted() {
        if (isActivityFirstStart)
            _loadedComponentAmounts.value = _loadedComponentAmounts.value!! + 1
    }

    //应用设置更改
    private val onSettingsChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "signature", "band", "character" -> getDailyTag()
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
    private val _dailyTag = MutableLiveData<DailyTag>()
    val dailyTag: LiveData<DailyTag>
        get() = _dailyTag
    fun getDailyTag() {
        viewModelScope.launch {
            val userName = Repository.getUserName()
            val preferenceBand = Repository.getPreferenceBand()
            val preferenceNearlyBandEvent = Repository.getBandEventByDate(
                date = systemDate.toDate(),
                character1Id = EventUtil.bandNameToCharacter1(preferenceBand)
            )
            val characterId = Repository.getPreferenceCharacter()
            val preferenceCharacter = Repository.getCharacterById(characterId)
            _dailyTag.value = DailyTag(userName, preferenceBand,
                preferenceNearlyBandEvent, preferenceCharacter)
        }
    }

    suspend fun getBandLastEventByDate(date: IntDate, character1Id: Int) =
        Repository.getBandLastEventByDate(date, character1Id)

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