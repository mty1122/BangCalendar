package com.mty.bangcalendar.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.util.CalendarUtil

class MainViewModel : ViewModel() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置
    private val systemDate = CalendarUtil()
    val todayEvent = Repository.getEventByDate(systemDate.getDate())


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

    init {
        _currentDate.value = CalendarUtil()
        _selectedItem.value = systemDate.day
    }

    private val searchDateLiveData = MutableLiveData<Int>()

    val event: LiveData<Event> = Transformations.switchMap(searchDateLiveData) {
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

}