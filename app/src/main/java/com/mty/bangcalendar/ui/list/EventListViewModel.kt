package com.mty.bangcalendar.ui.list

import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    suspend fun getEventList() = eventRepository.getEventList()

    suspend fun getEventPic(eventId: String) = eventRepository.getEventPic(eventId)

}