package com.mty.bangcalendar.logic.repository

import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.GuideInitData
import com.mty.bangcalendar.logic.model.IntDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GuideRepository @Inject constructor() {

    suspend fun fetchGuideInitData() = withContext(Dispatchers.IO) {
        GuideInitData(
            isFirstStart = PreferenceDao.isFirstStart,
            theme = PreferenceDao.getTheme(),
            lastRefreshDate = IntDate(PreferenceDao.getLastRefreshDate()),
            animPreference = PreferenceDao.getAnimPreference(),
            nvbarPreference = PreferenceDao.getNvbarPreference(),
            todayEvent = AppDatabase.getDatabase().eventDao()
                .getNearlyEventByDate(systemDate.toDate().value)!!
        )
    }

    suspend fun setDefaultPreference() = withContext(Dispatchers.IO) {
        PreferenceDao.setDefaultPreference()
    }

    suspend fun setNotFirstStart() = withContext(Dispatchers.IO) {
        PreferenceDao.isFirstStart = false
    }

}