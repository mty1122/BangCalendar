package com.mty.bangcalendar.util

import android.content.Context
import android.util.TypedValue
import com.mty.bangcalendar.R

object ThemeUtil {

    var currentTheme: Int? = null

    fun getThemeColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.app_theme_color, typedValue, true)
        return context.getColor(typedValue.resourceId)
    }

    fun getToolBarColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant ,
            typedValue, true)
        return context.getColor(typedValue.resourceId)
    }

    fun getBackgroundColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.app_background_color, typedValue, true)
        return context.getColor(typedValue.resourceId)
    }

    fun getDateTextColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.date_text_color, typedValue, true)
        return context.getColor(typedValue.resourceId)
    }

    fun setCurrentTheme(themeName: String) {
        when (themeName) {
            "theme_ppp" -> currentTheme = R.style.theme_ppp
            "theme_ag" -> currentTheme = R.style.theme_ag
            "theme_pp" -> currentTheme = R.style.theme_pp
            "theme_r" -> currentTheme = R.style.theme_r
            "theme_hhw" -> currentTheme = R.style.theme_hhw
            "theme_m" -> currentTheme = R.style.theme_m
            "theme_ras" -> currentTheme = R.style.theme_ras
            "theme_mg" -> currentTheme = R.style.theme_mg
        }
    }

}