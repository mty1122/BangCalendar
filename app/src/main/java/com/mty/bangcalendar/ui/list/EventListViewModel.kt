package com.mty.bangcalendar.ui.list

import android.graphics.drawable.Drawable
import androidx.lifecycle.*
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

    fun getEventPic(eventId: String, onPicReady: (Drawable) -> Unit) {
        viewModelScope.launch {
            Repository.getEventPic(eventId)?.let {
                onPicReady(it)
            }
        }
    }

}