package com.mty.bangcalendar.logic.model

import com.mty.bangcalendar.util.CalendarUtil

/**
 * @param value 纯数字组成的日期，格式为yyyymmdd，如20220830
 */
@JvmInline
value class IntDate(val value: Int) {

    /**
     * 该函数可以使两个IntDate值相减，得到两个日期天数的差值
     */
    operator fun minus(intDate: IntDate) = CalendarUtil(this) - CalendarUtil(intDate)

    fun getYear() = value / 10000
    fun getMonth() = value % 10000 / 100
    fun getDay() = value % 100

    fun toBirthday(): String {
        val birthday = value % 10000
        return if (birthday >= 1000)
            birthday.toString()
        else
            "0${birthday}"
    }

}