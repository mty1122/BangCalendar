package com.mty.bangcalendar.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.util.CalendarUtil

class MainViewModel : ViewModel() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置
    val systemDate = CalendarUtil()
    val todayEvent = Repository.getEventByDate(systemDate.getDate())

    //刷新用户昵称
    private val refreshUserNameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            getUserName()
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

    init {
        _currentDate.value = CalendarUtil()
        _selectedItem.value = systemDate.day

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.mty.bangcalendar.USERNAME_CHANGE")
        BangCalendarApplication.context.registerReceiver(refreshUserNameReceiver, intentFilter)
    }

    private val searchDateLiveData = MutableLiveData<Int>()

    val event: LiveData<Event?> = Transformations.switchMap(searchDateLiveData) {
        Repository.getEventByDate(it)
    }

    fun getEventByDate(date: Int) {
        searchDateLiveData.value = date
    }

    private val getEventPictureLiveData = MutableLiveData<String>()

    val eventPicture = Transformations.switchMap(getEventPictureLiveData) {
        Repository.getEventPicture(it)
    }

    fun getEventPicture(id: String) {
        getEventPictureLiveData.value = id
    }

    private val getCharacterByMonthLiveData = MutableLiveData<Int>()

    val characterInMonth = Transformations.switchMap(getCharacterByMonthLiveData) {
        Repository.getCharacterByMonth(it)
    }

    fun getCharacterByMonth(month: Int) {
        getCharacterByMonthLiveData.value = month
    }

    private val userNameLiveData = MutableLiveData<String>()

    val userName = Transformations.switchMap(userNameLiveData) {
        Repository.getUserName()
    }

    fun getUserName() {
        userNameLiveData.value = userNameLiveData.value
    }

    //取消注册Broadcast
    override fun onCleared() {
        super.onCleared()
        BangCalendarApplication.context.unregisterReceiver(refreshUserNameReceiver)
    }

}