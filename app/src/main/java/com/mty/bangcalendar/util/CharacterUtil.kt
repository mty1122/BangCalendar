package com.mty.bangcalendar.util

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

}