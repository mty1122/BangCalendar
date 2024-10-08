package com.mty.bangcalendar.util

import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.IntDate

object CharacterUtil {

    private fun birthdayToDay(birthday: String) = Integer.parseInt(birthday)  % 100

    fun characterListToBirthdayMap(characterList: List<Character>,
        mutableMap: MutableMap<String, Int>? = null): Map<String, Int> {
        val map = mutableMap ?: mutableMapOf()
        for (character in characterList) {
            map[birthdayToDay(character.birthday).toString()] = character.id.toInt()
        }
        return map
    }

    fun getNextBirthdayDate(birthday: String, systemDate: CalendarUtil): IntDate {
        val intBirthday = Integer.parseInt(birthday)
        val month = intBirthday / 100
        val day = intBirthday % 100
        val year = if (month > systemDate.month) systemDate.year
        else if (month < systemDate.month) systemDate.year + 1
        else {
            if (day < systemDate.day) systemDate.year + 1
            else systemDate.year
        }
        return CalendarUtil.getDate(year, month, day)
    }

    fun birthdayAway(birthday: String, systemDate: CalendarUtil): Int {
        val birthdayDate = getNextBirthdayDate(birthday, systemDate)
        return birthdayDate - systemDate.toDate()
    }

    fun matchCharacter(id: Int) =
        when (id) {
            1 -> R.drawable.bir_1
            2 -> R.drawable.bir_2
            3 -> R.drawable.bir_3
            4 -> R.drawable.bir_4
            5 -> R.drawable.bir_5
            6 -> R.drawable.bir_6
            7 -> R.drawable.bir_7
            8 -> R.drawable.bir_8
            9 -> R.drawable.bir_9
            10 -> R.drawable.bir_10
            11 -> R.drawable.bir_11
            12 -> R.drawable.bir_12
            13 -> R.drawable.bir_13
            14 -> R.drawable.bir_14
            15 -> R.drawable.bir_15
            16 -> R.drawable.bir_16
            17 -> R.drawable.bir_17
            18 -> R.drawable.bir_18
            19 -> R.drawable.bir_19
            20 -> R.drawable.bir_20
            21 -> R.drawable.bir_21
            22 -> R.drawable.bir_22
            23 ->  R.drawable.bir_23
            24 -> R.drawable.bir_24
            25 -> R.drawable.bir_25
            26 -> R.drawable.bir_26
            27 -> R.drawable.bir_27
            28 -> R.drawable.bir_28
            29 -> R.drawable.bir_29
            30 -> R.drawable.bir_30
            31 -> R.drawable.bir_31
            32 -> R.drawable.bir_32
            33 -> R.drawable.bir_33
            34 -> R.drawable.bir_34
            35 -> R.drawable.bir_35
            36 -> R.drawable.bir_36
            37 -> R.drawable.bir_37
            38 -> R.drawable.bir_38
            39 -> R.drawable.bir_39
            40 -> R.drawable.bir_40
            else -> -1
        }

}