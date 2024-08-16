package com.mty.bangcalendar.logic.model

import com.mty.bangcalendar.ui.main.state.DailyTagUiState
import com.mty.bangcalendar.ui.main.state.EventCardUiState
import kotlinx.coroutines.flow.Flow

data class MainViewInitData(
    val eventCardUiState: EventCardUiState,
    val dailyTagUiState: Flow<DailyTagUiState>,
    val birthdayCardUiState: Flow<Int>
)
