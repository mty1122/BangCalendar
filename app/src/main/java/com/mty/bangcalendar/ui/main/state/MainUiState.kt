package com.mty.bangcalendar.ui.main.state

import com.mty.bangcalendar.logic.model.Event

data class MainUiState(
    val isLoading: Boolean,
    val isFirstStart: Boolean,
    val todayEvent: Event?,
    val eventStartTime: Long,
    val eventEndTime: Long
)
