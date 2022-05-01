package com.mty.bangcalendar.logic.util

import java.util.Calendar

class CalendarUtil {

    private val calendar = Calendar.getInstance()

    var year: Int
    get() = calendar.get(Calendar.YEAR)
    set(value) = calendar.set(Calendar.YEAR, value)

    var month: Int
    get() = calendar.get(Calendar.MONTH) + 1
    set(value) = calendar.set(Calendar.MONTH, value)

    var day: Int
    get() = calendar.get(Calendar.DATE)
    set(value) = calendar.set(Calendar.DATE, value)

    init {
        calendar.firstDayOfWeek = Calendar.SUNDAY
    }

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

    fun clearDays() {
        calendar.set(Calendar.DATE, 1)
    }

    fun getDateList(): List<String> {
        val maxDays = getMaximumDaysInMonth()
        val dayOfWeak = getDayOfWeak()
        val dateList = ArrayList<String>()
        repeat(dayOfWeak - 1) {
            dateList.add("")
        }
        for (date in 1 until maxDays + 1) {
            dateList.add(date.toString())
        }
        return dateList
    }

    fun setRelativeMonth(month: Int) {
        calendar.add(Calendar.MONTH, month)
    }

    fun getCalendar(): Calendar {
        return calendar
    }

}