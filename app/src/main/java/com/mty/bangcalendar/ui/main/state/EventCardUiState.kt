package com.mty.bangcalendar.ui.main.state

import android.graphics.drawable.Drawable
import com.mty.bangcalendar.logic.model.Event
import kotlinx.coroutines.flow.Flow

data class EventCardUiState(
    val event: Event?,
    val eventPicture: Flow<Drawable?>?
)
