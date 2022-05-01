package com.mty.bangcalendar.logic.util

import com.mty.bangcalendar.R
import com.mty.bangcalendar.enum.EventConstant

object EventUtil {

    fun matchCharacter(character: Int): Int {
        when (character) {
            1 -> { return R.drawable.char_1 }
            2 -> { return R.drawable.char_2 }
            3 -> { return R.drawable.char_3 }
            4 -> { return R.drawable.char_4 }
            5 -> { return R.drawable.char_5 }
            6 -> { return R.drawable.char_6 }
            7 -> { return R.drawable.char_7 }
            8 -> { return R.drawable.char_8 }
            9 -> { return R.drawable.char_9 }
            10 -> { return R.drawable.char_10 }
            11 -> { return R.drawable.char_11 }
            12 -> { return R.drawable.char_12 }
            13 -> { return R.drawable.char_13 }
            14 -> { return R.drawable.char_14 }
            15 -> { return R.drawable.char_15 }
            16 -> { return R.drawable.char_16 }
            17 -> { return R.drawable.char_17 }
            18 -> { return R.drawable.char_18 }
            19 -> { return R.drawable.char_19 }
            20 -> { return R.drawable.char_20 }
            21 -> { return R.drawable.char_21 }
            22 -> { return R.drawable.char_22 }
            23 -> { return R.drawable.char_23 }
            24 -> { return R.drawable.char_24 }
            25 -> { return R.drawable.char_25 }
            26 -> { return R.drawable.char_26 }
            27 -> { return R.drawable.char_27 }
            28 -> { return R.drawable.char_28 }
            29 -> { return R.drawable.char_29 }
            30 -> { return R.drawable.char_30 }
            31 -> { return R.drawable.char_31 }
            32 -> { return R.drawable.char_32 }
            33 -> { return R.drawable.char_33 }
            34 -> { return R.drawable.char_34 }
            35 -> { return R.drawable.char_35 }
        }
        return -1
    }

    fun matchType(type: Int): String {
        when (type) {
            EventConstant.NORMAL.id -> return EventConstant.NORMAL.describe
            EventConstant.CHALLENGE.id -> return EventConstant.CHALLENGE.describe
            EventConstant.FIGHT.id -> return EventConstant.FIGHT.describe
            EventConstant.EX.id -> return EventConstant.EX.describe
            EventConstant.MISSION.id -> return EventConstant.MISSION.describe
            EventConstant.GROUP.id -> return EventConstant.GROUP.describe
            EventConstant.MULTI.id -> return EventConstant.MULTI.describe
        }
        return "检索不到活动类型"
    }

}