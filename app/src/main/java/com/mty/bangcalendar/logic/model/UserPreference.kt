package com.mty.bangcalendar.logic.model

import com.google.gson.annotations.SerializedName
import com.mty.bangcalendar.util.SecurityUtil

data class UserPreference(val phone: String, var name: String, var theme: String, var band: String,
                          @SerializedName("char_pref") var character: String,
                          val requestCode: String) {

    fun encrypt() {
        theme = SecurityUtil.encrypt(SecurityUtil.aesKey, theme)
        band = SecurityUtil.encrypt(SecurityUtil.aesKey, band)
        character = SecurityUtil.encrypt(SecurityUtil.aesKey, character)
    }

    fun decrypt() {
        theme = SecurityUtil.decrypt(SecurityUtil.aesKey, theme)
        band = SecurityUtil.decrypt(SecurityUtil.aesKey, band)
        character = SecurityUtil.decrypt(SecurityUtil.aesKey, character)
    }

}