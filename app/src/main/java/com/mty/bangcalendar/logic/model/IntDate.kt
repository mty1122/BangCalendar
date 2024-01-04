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

}