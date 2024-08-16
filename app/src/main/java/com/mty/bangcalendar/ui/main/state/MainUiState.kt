package com.mty.bangcalendar.ui.main.state

data class MainUiState(
    val isLoading: Boolean,
    val isFirstStart: Boolean,
    val shouldRecreate: Boolean,
)
