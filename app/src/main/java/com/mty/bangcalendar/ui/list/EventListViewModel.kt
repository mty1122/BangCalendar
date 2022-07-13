package com.mty.bangcalendar.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event

class EventListViewModel : ViewModel() {

    private val getEventListLiveData = MutableLiveData<Any?>()
    val eventList: LiveData<List<Event>> = Transformations.switchMap(getEventListLiveData) {
        Repository.getEventList()
    }
    fun getEventList() {
        getEventListLiveData.value = getEventListLiveData.value
    }

}