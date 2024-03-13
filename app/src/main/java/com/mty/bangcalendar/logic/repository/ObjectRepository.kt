package com.mty.bangcalendar.logic.repository

import com.mty.bangcalendar.logic.dao.PreferenceDao

object ObjectRepository {

    fun setFcmToken(token: String) {
        PreferenceDao.setFcmToken(token)
    }

    fun getAesKey() = PreferenceDao.aesKey

}