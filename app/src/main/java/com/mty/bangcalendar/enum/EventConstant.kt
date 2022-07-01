package com.mty.bangcalendar.enum

import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R

enum class EventConstant(val id: Int, val describe: String) {

    //活动属性
    PURE(1, BangCalendarApplication.context.getString(R.string.pure)),
    HAPPY(2, BangCalendarApplication.context.getString(R.string.happy)),
    COOL(3, BangCalendarApplication.context.getString(R.string.cool)),
    POWERFUL(4, BangCalendarApplication.context.getString(R.string.powerful)),

    //活动类型
    NORMAL(1, BangCalendarApplication.context.getString(R.string.normalLive)),
    CHALLENGE(2, BangCalendarApplication.context.getString(R.string.challengeLive)),
    FIGHT(3, BangCalendarApplication.context.getString(R.string.vsLive)),
    EX(4, BangCalendarApplication.context.getString(R.string.goalsLive)),
    MISSION(5, BangCalendarApplication.context.getString(R.string.missionLive)),
    GROUP(6, BangCalendarApplication.context.getString(R.string.groupLive)),
    MULTI(7, BangCalendarApplication.context.getString(R.string.multiVs))

}