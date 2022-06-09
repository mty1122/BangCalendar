package com.mty.bangcalendar.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Event

class SearchViewModel : ViewModel() {
    private val searchEventLiveData = MutableLiveData<Int>()
    val event: LiveData<Event?> = Transformations.switchMap(searchEventLiveData) {
        Repository.getEventById(it)
    }
    fun getEventById(id: Int) {
        searchEventLiveData.value = id
    }

    private val getEventPictureLiveData = MutableLiveData<String>()
    val eventPicture = Transformations.switchMap(getEventPictureLiveData) {
        Repository.getEventPicture(it)
    }
    fun getEventPicture(id: String) {
        getEventPictureLiveData.value = id
    }

    private val searchCharacterLiveData = MutableLiveData<String>()
    val character = Transformations.switchMap(searchCharacterLiveData) {
        Repository.getCharacterByName(it)
    }
    fun getCharacterByName(name: String) {
        searchCharacterLiveData.value = name
    }
}