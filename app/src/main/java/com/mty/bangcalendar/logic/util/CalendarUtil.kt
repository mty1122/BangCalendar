package com.mty.bangcalendar.logic.util

import java.util.*

class CalendarUtil {

    private val calendar = Calendar.getInstance()

    fun getMaximumDaysInMonth(): Int {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getDayOfWeak(): Int {
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun plusOneMonth() {
        calendar.add(Calendar.MONTH, 1)
    }

    fun minusOneMonth() {
        calendar.add(Calendar.MONTH, -1)
    }

    fun setRelativeMonth(month: Int) {
        calendar.add(Calendar.MONTH, month)
    }

    fun getCalendar(): Calendar {
        return calendar
    }

}