package com.mty.bangcalendar.util

import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.enum.EventConstant
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import java.util.regex.Pattern

object EventUtil {

    /**
     * 记录单次活动的时长，仅用于计算当前活动进度，对于已经结束和尚未开始的活动无效
     */
    private var eventLength: Long = 720000000
    lateinit var todayEvent: Event

    //默认活动天数
    const val DEFAULT_EVENT_LENGTH_DAYS = 8

    fun matchCharacter(character: Int?) =
        when (character) {
            1 -> R.drawable.char_1
            2 -> R.drawable.char_2
            3 -> R.drawable.char_3
            4 -> R.drawable.char_4
            5 -> R.drawable.char_5
            6 -> R.drawable.char_6
            7 -> R.drawable.char_7
            8 -> R.drawable.char_8
            9 -> R.drawable.char_9
            10 -> R.drawable.char_10
            11 -> R.drawable.char_11
            12 -> R.drawable.char_12
            13 -> R.drawable.char_13
            14 -> R.drawable.char_14
            15 -> R.drawable.char_15
            16 -> R.drawable.char_16
            17 -> R.drawable.char_17
            18 -> R.drawable.char_18
            19 -> R.drawable.char_19
            20 -> R.drawable.char_20
            21 -> R.drawable.char_21
            22 -> R.drawable.char_22
            23 ->  R.drawable.char_23
            24 -> R.drawable.char_24
            25 -> R.drawable.char_25
            26 -> R.drawable.char_26
            27 -> R.drawable.char_27
            28 -> R.drawable.char_28
            29 -> R.drawable.char_29
            30 -> R.drawable.char_30
            31 -> R.drawable.char_31
            32 -> R.drawable.char_32
            33 -> R.drawable.char_33
            34 -> R.drawable.char_34
            35 -> R.drawable.char_35
            36 -> R.drawable.char_36
            37 -> R.drawable.char_37
            38 -> R.drawable.char_38
            39 -> R.drawable.char_39
            40 -> R.drawable.char_40
            else -> null
        }

    fun matchType(type: Int) =
        when (type) {
            EventConstant.NORMAL.id -> EventConstant.NORMAL.describe
            EventConstant.CHALLENGE.id -> EventConstant.CHALLENGE.describe
            EventConstant.FIGHT.id -> EventConstant.FIGHT.describe
            EventConstant.EX.id -> EventConstant.EX.describe
            EventConstant.MISSION.id -> EventConstant.MISSION.describe
            EventConstant.GROUP.id -> EventConstant.GROUP.describe
            EventConstant.MULTI.id -> EventConstant.MULTI.describe
            else -> "检索不到活动类型"
        }

    fun matchAttrs(attrs: Int) =
        when (attrs) {
            EventConstant.PURE.id -> R.drawable.event_pure
            EventConstant.POWERFUL.id -> R.drawable.event_powerful
            EventConstant.COOL.id -> R.drawable.event_cool
            EventConstant.HAPPY.id -> R.drawable.event_happy
            else -> -1
        }

    fun getBand(event: Event) =
        //角色1 + 2 = 角色5 - 2 若角色5不存在则为 角色1 + 2 = 角色3
        if (event.character1 + 2 == ((event.character5?.minus(2)) ?: event.character3))
            when (event.character1) {
                1 -> EventConstant.PPP
                6 -> EventConstant.AG
                11 -> EventConstant.PP
                16 -> EventConstant.R
                21 -> EventConstant.HHW
                26 -> EventConstant.M
                31 -> EventConstant.RAS
                36-> EventConstant.MG
                else -> EventConstant.OTHER
            }
        else EventConstant.OTHER

    fun bandNameToCharacter1(bandName: String) =
        when (bandName) {
            EventConstant.PPP.describe -> 1
            EventConstant.AG.describe -> 6
            EventConstant.PP.describe -> 11
            EventConstant.R.describe -> 16
            EventConstant.HHW.describe -> 21
            EventConstant.M.describe -> 26
            EventConstant.RAS.describe -> 31
            EventConstant.MG.describe -> 36
            else -> -1
        }

    fun getBandPic(event: Event) =
        when (event.character7) {
            null -> if (event.character1 + 2 ==
                ((event.character5?.minus(2)) ?: event.character3)) {
                when (event.character1) {
                    1 -> R.drawable.logo_ppp
                    6 -> R.drawable.logo_ag
                    11 -> R.drawable.logo_pp
                    16 -> R.drawable.logo_r
                    21 -> R.drawable.logo_hhw
                    26 -> R.drawable.logo_m
                    31 -> R.drawable.logo_ras
                    36 -> R.drawable.logo_mg
                    else -> R.drawable.logo_bang
                }
            } else { R.drawable.logo_bang }

            else -> R.drawable.logo_all
    }

    fun eventIdFormat(id: Int) =
        when {
            id < 10 -> "00$id"
            id < 100 -> "0$id"
            else -> id.toString()
        }

    fun isSameEvent(eventCardTitle: String, eventId: Int?): Boolean {
        val regex = "\\d+"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(eventCardTitle)
        if (matcher.find()) {
            return matcher.group() == eventId.toString()
        }
        return false
    }

    fun getEventProgress(event: Event): Int {
        //活动编号与当前活动相同，即为当前活动
        if (event.id == todayEvent.id) {
            val startTime = getEventStartTime(event)
            val endTime = getEventEndTime(todayEvent)
            //若存在结束日期，则为非常规长度活动
            if (todayEvent.endDate != null) {
                //重新设置活动长度，用来计算活动进度
                eventLength = endTime - startTime
            }
            val systemTime = systemDate.getTimeInMillis()
            return when {
                systemTime < startTime -> 0
                systemTime >= endTime -> 100
                else -> {
                    //计算正在进行的活动进度
                    val eventFinishedTimes = (systemTime - startTime).toDouble()
                    ((eventFinishedTimes / eventLength) * 100).toInt()
                }
            }
        }
        //由于存在某个活动提前开始或者推迟开始的情况，因此如果不是当前活动，则比较开始日期
        return if (event.startDate < todayEvent.startDate) 100 else 0
    }

    private fun getEventStartTime(event: Event): Long =
        CalendarUtil(IntDate(event.startDate)).run {
        hour = 15
        getTimeInMillis()
        }


    private fun getEventEndTime(event: Event): Long =
        //若存在结束日期，则为非常规长度活动
        if (event.endDate != null)
            CalendarUtil(IntDate(event.endDate!!)).run {
                hour = 23
                getTimeInMillis()
            }
        else
            CalendarUtil(IntDate(event.startDate)).run {
                day += DEFAULT_EVENT_LENGTH_DAYS
                hour = 23
                getTimeInMillis()
            }

}