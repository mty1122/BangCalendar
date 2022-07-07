package com.mty.bangcalendar.util

import android.content.Context
import android.util.TypedValue
import com.mty.bangcalendar.R

object ThemeUtil {

    var currentTheme: Int? = null

    fun getThemeColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.app_theme_color, typedValue, true)
        return typedValue.resourceId
    }

    fun getDateTextColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.date_text_color, typedValue, true)
        return typedValue.resourceId
    }

    fun setCurrentTheme(themeName: String) {
        when (themeName) {
            "theme_ppp" -> currentTheme = R.style.theme_ppp
            "theme_r" -> currentTheme = R.style.theme_r
        }
    }

}