package com.mty.bangcalendar.enum

import com.mty.bangcalendar.BangCalendarApplication

enum class EventConstant(val id: Int, val describe: String) {

    //活动属性
    PURE(1, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.pure)),
    HAPPY(2, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.happy)),
    COOL(3, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.cool)),
    POWERFUL(4, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.powerful)),

    //活动类型
    NORMAL(1, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.normalLive)),
    CHALLENGE(2, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.challengeLive)),
    FIGHT(3, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.vsLive)),
    EX(4, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.goalsLive)),
    MISSION(5, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.missionLive)),
    GROUP(6, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.groupLive)),
    MULTI(7, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.multiVs)),

    //乐队
    PPP(1, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.ppp)),
    AG(2, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.ag)),
    PP(3, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.pp)),
    R(4, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.r)),
    HHW(5, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.hhw)),
    M(6, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.m)),
    RAS(7, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.ras)),
    OTHER(0, BangCalendarApplication.context.getString(com.mty.bangcalendar.R.string.other))

}