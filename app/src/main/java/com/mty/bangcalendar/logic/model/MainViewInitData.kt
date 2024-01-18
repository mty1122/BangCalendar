package com.mty.bangcalendar.logic.model

import android.graphics.drawable.Drawable
import com.mty.bangcalendar.ui.main.state.DailyTagUiState
import kotlinx.coroutines.flow.Flow

data class MainViewInitData(
    val currentEvent: Event?,
    val eventPicture: Flow<Drawable?>?,
    val dailyTagUiState: Flow<DailyTagUiState>,
    val birthdayCardUiState: Flow<Int>
)
