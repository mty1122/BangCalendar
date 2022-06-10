package com.mty.bangcalendar.logic.dao

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.mty.bangcalendar.BangCalendarApplication

object PreferenceDao {

    private fun sharedPreference() = BangCalendarApplication.context
        .getSharedPreferences("bang_calendar", Context.MODE_PRIVATE)

    fun isFirstStart(): Boolean = sharedPreference().run {
        val isFirstStart = this.getBoolean("isFirstStart", true)
        if (isFirstStart) {
            this.edit {
                putBoolean("isFirstStart", false)
            }
        }
        isFirstStart
    }

    fun getAdditionalTip(): String = sharedPreference().getString("additionalTip", "")!!

    fun setAdditionalTip(additionalTip: String) {
        sharedPreference().edit {
            putString("additionalTip", additionalTip)
        }
    }

    private fun defaultPreference() = PreferenceManager
        .getDefaultSharedPreferences(BangCalendarApplication.context)

    fun getUserName(): String = defaultPreference().getString("signature", "邦邦人")!!

    fun getPreferenceBand(): String = defaultPreference().getString("band", "other")!!

    fun getPreferenceCharacter(): Int =
        Integer.parseInt(defaultPreference().getString("character", "0")!!)

}