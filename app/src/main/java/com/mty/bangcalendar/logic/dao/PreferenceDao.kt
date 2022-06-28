package com.mty.bangcalendar.logic.dao

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.model.UserPreference

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

    fun getPhoneNum(): String = sharedPreference().getString("phone", "")!!

    fun setPhoneNum(phone: String) {
        sharedPreference().edit {
            putString("phone", phone)
        }
    }

    private fun defaultPreference() = PreferenceManager
        .getDefaultSharedPreferences(BangCalendarApplication.context)

    fun getUserName(): String = defaultPreference().getString("signature", "邦邦人")!!

    fun getPreferenceBand(): String = defaultPreference().getString("band", "other")!!

    private fun getTheme(): String = defaultPreference().getString("theme", "theme_ppp")!!

    fun getPreferenceCharacter(): Int =
        Integer.parseInt(defaultPreference().getString("character", "0")!!)

    fun getUserPreference(): UserPreference = UserPreference(getPhoneNum(), getUserName()
        , getTheme(), getPreferenceBand(), getPreferenceCharacter().toString())

    fun setUserPreference(userPreference: UserPreference) {
        defaultPreference().edit {
            putString("signature", userPreference.name)
            putString("theme", userPreference.theme)
            putString("band", userPreference.band)
            putString("character", userPreference.character)
        }
    }

}