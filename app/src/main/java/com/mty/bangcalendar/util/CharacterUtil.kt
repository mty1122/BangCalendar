package com.mty.bangcalendar.util

import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.model.Character
import java.util.TreeMap

object CharacterUtil {

    private fun birthdayToDay(birthday: String) = Integer.parseInt(birthday)  % 100

    fun characterListToBirthdayMap(characterList: List<Character>): Map<String, Int> {
        val map = TreeMap<String, Int>()
        for (character in characterList) {
            map[birthdayToDay(character.birthday).toString()] = character.id.toInt()
        }
        return map
    }

    fun birthdayToMonth(birthday: String) = Integer.parseInt(birthday) / 100

    fun getNextBirthdayDate(birthday: String, systemDate: CalendarUtil): Array<Int> {
        val dateArray = Array<Int>(2) { 0 }
        val intBirthday = Integer.parseInt(birthday)
        val month = intBirthday / 100
        val day = intBirthday % 100
        val year = if (month > systemDate.month) systemDate.year
        else if (month < systemDate.month) systemDate.year + 1
        else {
            if (day < systemDate.day) systemDate.year + 1
            else systemDate.year
        }
        val dateLater = CalendarUtil.getDate(year, month, day)
        dateArray[0] = dateLater
        val dateEarlier = CalendarUtil.getDate(systemDate.year,systemDate.month,systemDate.day)
        dateArray[1] = dateEarlier
        return dateArray
    }

    fun birthdayAway(birthday: String, systemDate: CalendarUtil): Int {
        val dateArray = getNextBirthdayDate(birthday, systemDate)
        val calendarUtilLater = CalendarUtil.dateToCalendarUtil(dateArray[0])
        val calendarUtilEarlier = CalendarUtil.dateToCalendarUtil(dateArray[1])
        return (calendarUtilLater - calendarUtilEarlier).toInt()
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
            else -> -1
        }

}