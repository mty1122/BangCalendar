package com.mty.bangcalendar.util

object SecurityUtil {

    init {
        System.loadLibrary("bangcalendar")
    }

    external fun getRequestCode(): String

    external fun decrypt(text: String): String

}