package com.mty.bangcalendar.logic.model

import android.view.View

//记录参与滚动的view的上个位置
class CalendarScrollView(val view: View, var lastPosition: Int)