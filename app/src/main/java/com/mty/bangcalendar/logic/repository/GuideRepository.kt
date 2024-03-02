package com.mty.bangcalendar.logic.repository

import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.GuideInitData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GuideRepository @Inject constructor() {

    suspend fun fetchGuideInitData() = withContext(Dispatchers.IO) {
        val isFirstStart = PreferenceDao.isFirstStart
        val theme = PreferenceDao.getTheme()
        val lastRefreshDay = PreferenceDao.getLastRefreshDay()
        val animPreference = PreferenceDao.getAnimPreference()
        val nvbarPreference = PreferenceDao.getNvbarPreference()
        GuideInitData(isFirstStart, theme, lastRefreshDay, animPreference, nvbarPreference)
    }

    suspend fun setDefaultPreference() = withContext(Dispatchers.IO) {
        PreferenceDao.setDefaultPreference()
    }

    suspend fun setNotFirstStart() = withContext(Dispatchers.IO) {
        PreferenceDao.isFirstStart = false
    }

}