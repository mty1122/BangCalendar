package com.mty.bangcalendar.logic.dao

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.util.SecurityUtil

object PreferenceDao {

    private fun sharedPreference() = BangCalendarApplication.context
        .getSharedPreferences("bang_calendar", Context.MODE_PRIVATE)

    var isFirstStart: Boolean
        get() = sharedPreference().getBoolean("isFirstStart", true)
        set(value) = sharedPreference().edit { putBoolean("isFirstStart", value) }

    var aesKey: String
        get() = sharedPreference().getString("aesKey", "")!!
        set(value) = sharedPreference().edit { putString("aesKey", value) }

    fun getLastRefreshDay(): Int = sharedPreference().getInt("lastRefreshDay", 0)

    fun setLastRefreshDay(day: Int) {
        sharedPreference().edit {
            putInt("lastRefreshDay", day)
        }
    }

    fun getPhoneNum(): String = sharedPreference().getString("phone", "")!!

    fun setPhoneNum(phone: String) {
        sharedPreference().edit {
            putString("phone", phone)
        }
    }

    fun setFcmToken(token: String) {
        sharedPreference().edit {
            putString("fcm_token", token)
        }
    }

    private fun defaultPreference() = PreferenceManager
        .getDefaultSharedPreferences(BangCalendarApplication.context)

    fun getUserName(): String = defaultPreference().getString("signature", "邦邦人")!!

    fun getPreferenceBand(): String = defaultPreference().getString("band", "other")!!

    fun getTheme(): String = defaultPreference().getString("theme", "theme_m")!!

    fun getPreferenceCharacter(): Int =
        Integer.parseInt(defaultPreference().getString("character", "0")!!)

    fun getUserPreference(): UserPreference = UserPreference(getPhoneNum(), getUserName(),
        getTheme(), getPreferenceBand(), getPreferenceCharacter().toString(),
        SecurityUtil.encrypt(SecurityUtil.aesKey, getPhoneNum()))

    fun setUserPreference(userPreference: UserPreference) {
        defaultPreference().edit {
            putString("signature", userPreference.name)
            putString("theme", userPreference.theme)
            putString("band", userPreference.band)
            putString("character", userPreference.character)
        }
    }

    fun getAnimPreference(): Boolean = defaultPreference().getBoolean("anim", false)
    fun getNvbarPreference(): Boolean = defaultPreference().getBoolean("nvbar", false)

    fun setDefaultPreference() {
        defaultPreference().edit {
            putString("band", "m")
            putString("character", "26")
        }
    }

    fun registerOnDefaultPreferenceChangeListener(listener:
        SharedPreferences.OnSharedPreferenceChangeListener) {
        defaultPreference().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnDefaultPreferenceChangeListener(listener:
        SharedPreferences.OnSharedPreferenceChangeListener) {
        defaultPreference().unregisterOnSharedPreferenceChangeListener(listener)
    }

}