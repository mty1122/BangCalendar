package com.mty.bangcalendar.ui.main.state

import com.mty.bangcalendar.logic.model.IntDate

data class CalendarItemUiState(
    val isVisible: Boolean,
    val dateList: List<String>,
    val birthdayMap: Map<String, Int>,
    val getCurrentDate: () -> IntDate,
    val onDateChange: (IntDate) -> Unit
)
