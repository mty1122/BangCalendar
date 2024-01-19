package com.mty.bangcalendar.ui.main.state

data class CalendarItemUiState(
    val selectedItem: Int = -1, //默认不选中
    val dateList: List<String>,
    val birthdayMap: Map<String, Int>,
    val onClick: () -> Unit = {} //默认不设置点击事件
)
