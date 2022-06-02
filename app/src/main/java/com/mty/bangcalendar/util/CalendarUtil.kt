package com.mty.bangcalendar.util

import java.util.Calendar

class CalendarUtil {

    companion object {

        const val FIVE_ROWS = 5
        const val SIX_ROWS = 6

        private fun dateToCalendarUtil(date: Int) = CalendarUtil().apply {
            clear()
            year = date / 10000
            month = date % 10000 / 100
            day = date % 100
        }

        fun differentOfTwoDates(date1: Int, date2: Int): Long {
            val timeStart = dateToCalendarUtil(date1).getTimeInMillis()
            val timeEnd = dateToCalendarUtil(date2).getTimeInMillis()
            return (timeEnd - timeStart) / (1000 * 3600 * 24)
        }

    }

    private val calendar = Calendar.getInstance()

    var rows: Int = FIVE_ROWS

    var year: Int
    get() = calendar.get(Calendar.YEAR)
    set(value) = calendar.set(Calendar.YEAR, value)

    //月默认从0开始
    var month: Int
    get() = calendar.get(Calendar.MONTH) + 1
    set(value) = calendar.set(Calendar.MONTH, value - 1)

    var day: Int
    get() = calendar.get(Calendar.DATE)
    set(value) = calendar.set(Calendar.DATE, value)

    init {
        calendar.firstDayOfWeek = Calendar.SUNDAY
    }

    fun getMaximumDaysInMonth(): Int {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getDayOfWeak(): Int {
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun plusOneMonth() {
        calendar.add(Calendar.MONTH, 1)
        refreshRows()
    }

    fun minusOneMonth() {
        calendar.add(Calendar.MONTH, -1)
        refreshRows()
    }

    fun clearDays() {
        calendar.set(Calendar.DATE, 1)
    }

    fun clear() {
        calendar.clear()
    }

    fun getTimeInMillis() = calendar.timeInMillis

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

    fun refreshRows() {
        val maxDays = getMaximumDaysInMonth()
        val dayOfWeak = getDayOfWeak()
        rows = if (maxDays + dayOfWeak < 37) {
            FIVE_ROWS
        } else {
            SIX_ROWS
        }
        LogUtil.d("CalendarUtil",
            "maxDays = $maxDays  dayOfWeek = $dayOfWeak rows = $rows")
    }

    fun setRelativeMonth(month: Int) {
        calendar.add(Calendar.MONTH, month)
    }

    fun getDate(): Int {
        val year = year * 10000
        val month = month * 100
        val day = day
        return year + month + day
    }

    fun getTimeName(): String {
        val time = calendar.get(Calendar.HOUR_OF_DAY)
        return when (true) {
            (time in 0..6 || time in 19..24) -> "晚上"
            (time in 6..11) -> "上午"
            (time in 11..13) -> "中午"
            (time in 13..19) -> "下午"
            else -> ""
        }
    }

}