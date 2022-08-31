package com.mty.bangcalendar.util

import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.enum.EventConstant
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import java.util.regex.Pattern

object EventUtil {

    private const val EVENT_LENGTH: Long = 460800000

    fun matchCharacter(character: Int) =
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
            else -> -1
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

    fun getAttrsName(attrs: Int) =
        when (attrs) {
            EventConstant.PURE.id -> EventConstant.PURE.describe
            EventConstant.POWERFUL.id -> EventConstant.POWERFUL.describe
            EventConstant.COOL.id -> EventConstant.COOL.describe
            EventConstant.HAPPY.id -> EventConstant.HAPPY.describe
            else -> -1
        }

    fun getBand(event: Event) =
        if (event.character1 + 4 == event.character5)
            when (event.character1) {
                1 -> EventConstant.PPP
                6 -> EventConstant.AG
                11 -> EventConstant.PP
                16 -> EventConstant.R
                21 -> EventConstant.HHW
                26 -> EventConstant.M
                31 -> EventConstant.RAS
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
            else -> -1
        }

    fun getBandPic(event: Event) =
        when (event.character7) {
            -1 -> if (event.character1 + 4 == event.character5) {
                when (event.character1) {
                    1 -> R.drawable.logo_ppp
                    6 -> R.drawable.logo_ag
                    11 -> R.drawable.logo_pp
                    16 -> R.drawable.logo_r
                    21 -> R.drawable.logo_hhw
                    26 -> R.drawable.logo_m
                    31 -> R.drawable.logo_ras
                    else -> R.drawable.logo_bang
                }
            } else { R.drawable.logo_bang }

            else -> R.drawable.logo_all
    }

    fun eventIdFormat(id: Int) =
        when (true) {
            (id < 10) -> "00$id"
            (id < 100) -> "0$id"
            else -> id.toString()
        }

    fun isSameEvent(binding: ActivityMainBinding, eventId: Int): Boolean {
        val eventCardTitle = binding.eventCard.eventType.text
        val regex = "\\d+"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(eventCardTitle)
        if (matcher.find()) {
            return matcher.group() == eventId.toString()
        }
        return false
    }

    fun getEventStartTime(event: Event?): Long? =
        if (event != null)
            CalendarUtil(IntDate(event.startDate)).run {
                hour = 15
                getTimeInMillis()
            }
        else null


    fun getEventEndTime(event: Event?): Long? =
        if (event != null)
            CalendarUtil(IntDate(event.startDate)).run {
                day += 5
                hour = 23
                getTimeInMillis()
            }
        else null

    fun getEventProgress(systemTime: Long, eventStartTime: Long): Int {
        val eventFinishedTimes = (systemTime - eventStartTime).toDouble()
        val eventProgress = ((eventFinishedTimes / EVENT_LENGTH) * 100).toInt()
        LogUtil.d("EventUtil", "eventProgress = $eventProgress")
        return eventProgress
    }

}