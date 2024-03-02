package com.mty.bangcalendar.logic

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.room.withTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.GetPreferenceRequest
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.SmsRequest
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.logic.network.BangCalendarNetwork
import com.mty.bangcalendar.logic.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Repository {

    private val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    fun getCharacterJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("character.json")

    fun getEventJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("event.json")

    suspend fun withDatabaseTransaction(block: suspend () -> Unit) {
        AppDatabase.getDatabase().withTransaction {
            block()
        }
    }

    suspend fun addCharacterToDatabase(character: Character) {
        AppDatabase.getDatabase().characterDao().insertCharacter(character)
    }

    suspend fun addEventToDatabase(event: Event) {
        AppDatabase.getDatabase().eventDao().insertEvent(event)
    }

    suspend fun addCharacterListToDatabase(characterList: List<Character>) {
        AppDatabase.getDatabase().characterDao().insertAll(characterList)
    }

    suspend fun addEventListToDatabase(eventList: List<Event>) {
        AppDatabase.getDatabase().eventDao().insertAll(eventList)
    }

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

    fun getCharacterListFromInternet() = BangCalendarNetwork.getCharacterList()

    fun getEventListFromInternet() = BangCalendarNetwork.getEventList()

    suspend fun sendSms(request: SmsRequest) = withContext(Dispatchers.IO) {
        try {
            val result = BangCalendarNetwork.sendSms(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest) = withContext(Dispatchers.IO) {
        try {
            val result = BangCalendarNetwork.login(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPhoneNum() = withContext(Dispatchers.IO) {
        PreferenceDao.getPhoneNum()
    }

    suspend fun setPhoneNum(phone: String) = withContext(Dispatchers.IO) {
        PreferenceDao.setPhoneNum(phone)
    }

    suspend fun downloadUserPreference(request: GetPreferenceRequest) = withContext(Dispatchers.IO) {
        try {
            val result = BangCalendarNetwork.getUserPreference(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadUserPreference(userPreference: UserPreference) = withContext(Dispatchers.IO) {
        try {
            val result = BangCalendarNetwork.setUserPreference(userPreference)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPreference() = withContext(Dispatchers.IO) {
        PreferenceDao.getUserPreference()
    }

    suspend fun setUserPreference(userPreference: UserPreference) = withContext(Dispatchers.IO) {
        PreferenceDao.setUserPreference(userPreference)
    }

    suspend fun getEventList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getEventList()
    }

    suspend fun getCharacterList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().characterDao().getCharacterList()
    }

    fun setLastRefreshDay(day: Int) {
        PreferenceDao.setLastRefreshDay(day)
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

    suspend fun getAppUpdateInfo() = withContext(Dispatchers.IO) {
        try {
            val updateInfo = BangCalendarNetwork.getAppUpdateInfo()
            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAesKey() = PreferenceDao.aesKey

    fun setAesKey(key: String) {
        PreferenceDao.aesKey = key
    }

}