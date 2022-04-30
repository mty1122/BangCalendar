package com.mty.bangcalendar.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.mty.bangcalendar.BangCalendarApplication

object PreferenceDao {

    private fun sharedPreference() = BangCalendarApplication.context
        .getSharedPreferences("bang_calendar", Context.MODE_PRIVATE)

    fun isFirstStart(): Boolean = sharedPreference().run {
            val isFirstStart = this.getBoolean("isFirstStart", true)
            this.edit {
                putBoolean("isFirstStart", false)
            }
            isFirstStart
        }

}