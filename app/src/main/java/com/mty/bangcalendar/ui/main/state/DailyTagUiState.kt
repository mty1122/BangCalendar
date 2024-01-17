package com.mty.bangcalendar.ui.main.state

import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event

data class DailyTagUiState(
    val userName: String,
    val preferenceBand: String,
    val preferenceBandNextEvent: Event?,
    val preferenceBandLatestEvent: Event?,
    val preferenceCharacter: Character?,
)

//用户偏好不存在，不启动dailytag
fun DailyTagUiState.shouldVisible() = preferenceBandNextEvent != null || preferenceCharacter != null
fun DailyTagUiState.shouldCharacterItemVisible() = preferenceCharacter != null
fun DailyTagUiState.shouldBandItemVisible() = preferenceBandNextEvent != null
