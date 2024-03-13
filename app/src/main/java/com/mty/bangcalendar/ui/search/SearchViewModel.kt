package com.mty.bangcalendar.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.repository.CharacterRepository
import com.mty.bangcalendar.logic.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _eventLiveData = MutableLiveData<Event>()
    val eventLiveData: LiveData<Event>
        get() = _eventLiveData
    fun getEventById(id: Int) {
        viewModelScope.launch {
            _eventLiveData.value = eventRepository.getEventById(id)
        }
    }

    private val _characterLiveData = MutableLiveData<Character>()
    val characterLiveData: LiveData<Character>
        get() = _characterLiveData
    fun getCharacterByName(name: String) {
        viewModelScope.launch {
            _characterLiveData.value = characterRepository.getCharacterByName(name)
        }
    }

    fun getEventPic(eventId: String) = eventRepository.getEventPic(eventId)

}