package com.mty.bangcalendar.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.enum.IntentActions
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.model.MainViewInitData
import com.mty.bangcalendar.logic.repository.CharacterRepository
import com.mty.bangcalendar.logic.repository.EventRepository
import com.mty.bangcalendar.logic.repository.PreferenceRepository
import com.mty.bangcalendar.ui.main.state.DailyTagUiState
import com.mty.bangcalendar.ui.main.state.EventCardUiState
import com.mty.bangcalendar.ui.main.state.MainUiState
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.CharacterUtil
import com.mty.bangcalendar.util.EventUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val characterRepository: CharacterRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    //主界面状态
    private val _mainUiState =  MutableStateFlow(
        MainUiState(
            isLoading = true,
            isFirstStart = true,
            shouldRecreate = false,
            todayEvent = null,
            eventStartTime = 0,
            eventEndTime = 0
    ))
    val mainUiState = _mainUiState.asStateFlow()
    fun startLoading() {
        _mainUiState.update {
            it.copy(isLoading = true)
        }
    }
    fun loadCompleted() {
        _mainUiState.update {
            it.copy(
                isLoading = false,
                isFirstStart = false
            )
        }
    }
    private fun recreateActivity() {
        _mainUiState.update {
            it.copy(shouldRecreate = true)
        }
    }
    fun recreateActivityCompleted() {
        _mainUiState.update {
            it.copy(shouldRecreate = false)
        }
    }
    private fun setTodayEventState(todayEvent: Event) {
        val eventStartTime = EventUtil.getEventStartTime(todayEvent)
        val eventEndTime: Long
        if (todayEvent.endDate != null) {
            eventEndTime =
                EventUtil.getEventEndTime(IntDate(todayEvent.endDate!!))
            EventUtil.setEventLength(eventEndTime - eventStartTime)
        } else {
            eventEndTime = EventUtil.getEventEndTime(todayEvent)
        }
        _mainUiState.update {
            it.copy(
                todayEvent = todayEvent,
                eventStartTime = eventStartTime,
                eventEndTime = eventEndTime
            )
        }
    }

    //主界面加载的起点，首次启动使用当天活动
    fun fetchInitData() = flow {
        val currentEvent = if (mainUiState.value.isFirstStart) {
            val todayEvent = eventRepository.getEventByDate(systemDate.toDate())!!
            setTodayEventState(todayEvent)
            todayEvent
        } else {
            eventCardUiState.value!!.event
        }
        val eventPicture: Flow<Drawable?>? = if (currentEvent != null) {
            val eventId = EventUtil.eventIdFormat(currentEvent.id.toInt())
            eventRepository.getEventPic(eventId)
        }  else {
            null
        }
        val dailyTagUiState = fetchDailyTagUiState()
        val birthdayCardUiState = if (mainUiState.value.isFirstStart) flow {
            emit(
                characterRepository.getCharacterIdByBirthday(systemDate.toDate().toBirthday()).toInt()
            )
        } else flow {
            emit(
                characterRepository.getCharacterIdByBirthday(currentDate.value!!.toBirthday()).toInt()
            )
        }
        emit(
            MainViewInitData(currentEvent, eventPicture, dailyTagUiState, birthdayCardUiState)
        )
    }

    //当前选中的日期，处理因日期变化带来的ui改变（核心功能）
    val currentDate: LiveData<IntDate>
        get() = _currentDate

    private val _currentDate = MutableLiveData(CalendarUtil().toDate())
    fun refreshCurrentDate(newDate: IntDate) {
        //防止重复刷新
        if (newDate != currentDate.value) {
            _currentDate.value = newDate
        }
    }

    //生日卡片模块
    val birthdayCardUiState: LiveData<Int>
        get() = _birthdayCardUiState
    private val _birthdayCardUiState = MutableLiveData<Int>()
    fun refreshBirthdayCard(date: IntDate) {
        viewModelScope.launch {
            val characterId = characterRepository.getCharacterIdByBirthday(date.toBirthday())
                .toInt()
            if (characterId != _birthdayCardUiState.value)
                _birthdayCardUiState.value = characterId
        }
    }
    suspend fun fetchBirthdayMapByMonth(month: Int): Map<String, Int> {
        val characterList = characterRepository.getCharacterByMonth(month)
        return CharacterUtil.characterListToBirthdayMap(characterList)
    }

    //外部日期跳转请求
    val jumpDate: LiveData<IntDate>
        get() = _jumpDate
    private val _jumpDate = MutableLiveData<IntDate>()
    fun setJumpDate(startDate: IntDate) {
        _jumpDate.value = startDate
    }

    //活动卡片模块
    private val _eventCardUiState = MutableLiveData<EventCardUiState>()
    val eventCardUiState: LiveData<EventCardUiState>
        get() = _eventCardUiState
    fun getEventByDate(date: IntDate) {
        viewModelScope.launch {
            val event = eventRepository.getEventByDate(date)
            if (event == null) {
                _eventCardUiState.value = EventCardUiState(null, null)
            } else {
                val eventId = EventUtil.eventIdFormat(event.id.toInt())
                val eventPicture = eventRepository.getEventPic(eventId)
                _eventCardUiState.value = EventCardUiState(event, eventPicture)
            }
        }
    }

    //dailyTag模块
    private val _dailyTagUiState = MutableLiveData<DailyTagUiState>()
    val dailyTagUiState: LiveData<DailyTagUiState>
        get() = _dailyTagUiState
    private fun refreshDailyTag() {
        viewModelScope.launch {
            fetchDailyTagUiState().collect{
                _dailyTagUiState.value = it
            }
        }
    }
    private fun fetchDailyTagUiState() = flow {
        val userName = preferenceRepository.getUserName()
        val preferenceBand = preferenceRepository.getPreferenceBand()
        val preferenceBandNextEvent = eventRepository.getBandEventByDate(
            date = systemDate.toDate(),
            character1Id = EventUtil.bandNameToCharacter1(preferenceBand)
        )
        val preferenceBandLatestEvent = eventRepository.getBandLastEventByDate(
            date = systemDate.toDate(),
            character1Id = preferenceBandNextEvent?.character1
        )
        val characterId = preferenceRepository.getPreferenceCharacter()
        val preferenceCharacter = characterRepository.getCharacterById(characterId)
        emit(
            DailyTagUiState(userName, preferenceBand,
            preferenceBandNextEvent, preferenceBandLatestEvent, preferenceCharacter)
        )
    }

    //监听跳转日期请求广播
    private val jumpDateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val startDate = intent.getIntExtra("current_start_date", -1)
            if (startDate != -1)
                setJumpDate(IntDate(startDate))
        }
    }
    //监听应用设置更改
    private val onSettingsChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "signature", "band", "character" -> refreshDailyTag()
                "theme", "nvbar" ->recreateActivity()
            }
        }
    init {
        preferenceRepository.registerOnDefaultPreferenceChangeListener(onSettingsChangeListener)

        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentActions.JUMP_DATE_ACTION.value)
        ContextCompat.registerReceiver(BangCalendarApplication.context, jumpDateReceiver,
            intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }
    //取消注册监听器
    override fun onCleared() {
        super.onCleared()
        preferenceRepository.unregisterOnDefaultPreferenceChangeListener(onSettingsChangeListener)
        BangCalendarApplication.context.unregisterReceiver(jumpDateReceiver)
    }

}