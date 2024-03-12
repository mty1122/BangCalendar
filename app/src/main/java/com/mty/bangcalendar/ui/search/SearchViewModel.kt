package com.mty.bangcalendar.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _eventLiveData = MutableLiveData<Event>()
    val eventLiveData: LiveData<Event>
        get() = _eventLiveData
    fun getEventById(id: Int) {
        viewModelScope.launch {
            _eventLiveData.value = Repository.getEventById(id)
        }
    }

    private val _characterLiveData = MutableLiveData<Character>()
    val characterLiveData: LiveData<Character>
        get() = _characterLiveData
    fun getCharacterByName(name: String) {
        viewModelScope.launch {
            _characterLiveData.value = Repository.getCharacterByName(name)
        }
    }

    fun getEventPic(eventId: String) = Repository.getEventPic(eventId)

}