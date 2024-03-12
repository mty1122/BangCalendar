package com.mty.bangcalendar.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import kotlinx.coroutines.launch

class EventListViewModel : ViewModel() {

    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>>
        get() = _eventList
    fun getEventList() {
        viewModelScope.launch {
           _eventList.value = Repository.getEventList()
        }
    }

    fun getEventPic(eventId: String) = Repository.getEventPic(eventId)

}