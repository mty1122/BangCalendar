package com.mty.bangcalendar.ui.list

import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EventListViewModel : ViewModel() {

    private val getEventListLiveData = MutableLiveData<Any?>()
    val eventList: LiveData<List<Event>> = Transformations.switchMap(getEventListLiveData) {
        Repository.getEventList()
    }
    fun getEventList() {
        getEventListLiveData.value = getEventListLiveData.value
    }

    fun getEventPic(eventId: String, onPicReady: (Drawable) -> Unit) {
        viewModelScope.launch {
            Repository.getEventPic(eventId)?.let {
                onPicReady(it)
            }
            cancel()
        }
    }

}