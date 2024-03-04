package com.mty.bangcalendar.ui.settings.state

import com.mty.bangcalendar.logic.model.IntDate

data class SettingsUiState(
    val phoneNumber: String = "",
    val hasNewVersion: Boolean = false,
    val newVersionName: String = "",
    val lastRefreshDate: IntDate? = null
)
