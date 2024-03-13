package com.mty.bangcalendar.logic.repository

import android.content.SharedPreferences
import com.mty.bangcalendar.logic.dao.PreferenceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreferenceRepository @Inject constructor() {

    suspend fun getUserName() = withContext(Dispatchers.IO) {
        PreferenceDao.getUserName()
    }

    suspend fun getPreferenceBand() = withContext(Dispatchers.IO) {
        PreferenceDao.getPreferenceBand()
    }

    suspend fun getPreferenceCharacter() = withContext(Dispatchers.IO) {
        PreferenceDao.getPreferenceCharacter()
    }

    fun registerOnDefaultPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        PreferenceDao.registerOnDefaultPreferenceChangeListener(listener)
    }

    fun unregisterOnDefaultPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        PreferenceDao.unregisterOnDefaultPreferenceChangeListener(listener)
    }

}