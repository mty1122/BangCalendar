package com.mty.bangcalendar.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>>
        get() = _eventList
    fun getEventList() {
        viewModelScope.launch {
           _eventList.value = eventRepository.getEventList()
        }
    }

    suspend fun getEventPic(eventId: String) = eventRepository.getEventPic(eventId)

}