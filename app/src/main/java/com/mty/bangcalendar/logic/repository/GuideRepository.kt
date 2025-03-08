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
            nvbarPreference = PreferenceDao.getNvbarPreference()
        )
    }

    suspend fun setDefaultPreference() = withContext(Dispatchers.IO) {
        PreferenceDao.setDefaultPreference()
    }

    suspend fun setNotFirstStart() = withContext(Dispatchers.IO) {
        PreferenceDao.isFirstStart = false
    }

    // 今日活动不并入初始数据是因为存在空指针风险，初次启动无法查询到今日活动，因此需要在数据库加载完成后查找
    suspend fun getTodayEvent() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getNearlyEventByDate(systemDate.toDate().value)!!
    }

}