package com.mty.bangcalendar.logic

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Repository {

    private val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    suspend fun getUserName() = withContext(Dispatchers.IO) {
        PreferenceDao.getUserName()
    }

    suspend fun getPreferenceBand() = withContext(Dispatchers.IO) {
        PreferenceDao.getPreferenceBand()
    }

    suspend fun getPreferenceCharacter() = withContext(Dispatchers.IO) {
        PreferenceDao.getPreferenceCharacter()
    }

    suspend fun getEventByDate(date: IntDate) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getNearlyEventByDate(date.value)
    }

    suspend fun getEventById(id: Int) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getEventById(id)
    }

    suspend fun getBandEventByDate(date: IntDate, character1Id: Int) = withContext(Dispatchers.IO) {
        if (character1Id < 1)
            null
        else
            AppDatabase.getDatabase().eventDao().getNearlyBandEventByDate(date.value, character1Id)
    }

    suspend fun getBandLastEventByDate(date: IntDate, character1Id: Int?) =
        withContext(Dispatchers.IO) {
            if (character1Id != null)
                AppDatabase.getDatabase().eventDao()
                    .getLastNearlyBandEventByDate(date.value, character1Id)
            else
                null
    }

   suspend fun getCharacterByMonth(month: Int) = withContext(Dispatchers.IO) {
        val formatMonth = if (month < 10) "0$month"
                          else month.toString()
       AppDatabase.getDatabase().characterDao().getCharacterByMonth(formatMonth)
    }

    suspend fun getCharacterIdByBirthday(birthday: String) = withContext(Dispatchers.IO) {
        val idList = AppDatabase.getDatabase().characterDao().getCharacterIdByBirthday(birthday)
        if (idList.isEmpty()) 0 else idList[0]
    }

    suspend fun getCharacterById(id: Int) = withContext(Dispatchers.IO) {
        if (id < 1)
            null
        else
            AppDatabase.getDatabase().characterDao().getCharacterById(id)
    }

    suspend fun getCharacterByName(name: String) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().characterDao().getCharacterByName(name)
    }

    suspend fun getEventList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getEventList()
    }

    suspend fun getCharacterList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().characterDao().getCharacterList()
    }

    suspend fun getEventPic(eventId: String) = suspendCoroutine {
        val uri = Uri.parse(
            ServiceCreator.BASE_URL + "event/banner_memorial_event$eventId.png")
        Glide.with(BangCalendarApplication.context).load(uri).apply(glideOptions)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    it.resume(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    it.resume(null)
                }
            })
    }

    fun setFcmToken(token: String) {
        PreferenceDao.setFcmToken(token)
    }

    fun registerOnDefaultPreferenceChangeListener(listener:
        SharedPreferences.OnSharedPreferenceChangeListener) {
        PreferenceDao.registerOnDefaultPreferenceChangeListener(listener)
    }

    fun unregisterOnDefaultPreferenceChangeListener(listener:
        SharedPreferences.OnSharedPreferenceChangeListener) {
        PreferenceDao.unregisterOnDefaultPreferenceChangeListener(listener)
    }

    fun getAesKey() = PreferenceDao.aesKey

}