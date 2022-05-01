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
    val systemDate = CalendarUtil()


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

    //判断是否首次启动
    val event: LiveData<Event> = Transformations.switchMap(searchDateLiveData) {
        Repository.getEventByDate(it)
    }

    //调用该方法使isFirstStartLiveData的值改变从而调用Repository.isFirstStart()方法
    fun getEventByDate(date: Int) {
        searchDateLiveData.value = date
    }

}