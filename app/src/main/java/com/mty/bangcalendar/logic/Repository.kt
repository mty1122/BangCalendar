package com.mty.bangcalendar.logic

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.*
import com.mty.bangcalendar.logic.network.BangCalendarNetwork
import com.mty.bangcalendar.logic.network.ServiceCreator
import com.mty.bangcalendar.logic.model.IntDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Repository {

    private val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    fun getCharacterJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("character.json")

    fun getEventJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("event.json")

    fun addCharacterToDatabase(character: Character) {
        AppDatabase.getDatabase().characterDao().insertCharacter(character)
    }

    fun addEventToDatabase(event: Event) {
        AppDatabase.getDatabase().eventDao().insertEvent(event)
    }

    suspend fun getGuideInitData() = withContext(Dispatchers.IO) {
        val isFirstStart = PreferenceDao.isFirstStart()
        val theme = PreferenceDao.getTheme()
        val lastRefreshDay = PreferenceDao.getLastRefreshDay()
        GuideInitData(isFirstStart, theme, lastRefreshDay)
    }

    suspend fun getAdditionalTip() = withContext(Dispatchers.IO) {
        PreferenceDao.getAdditionalTip()
    }

    suspend fun setAdditionalTip(additionalTip: String) = withContext(Dispatchers.IO) {
        PreferenceDao.setAdditionalTip(additionalTip)
        PreferenceDao.getAdditionalTip()
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
        AppDatabase.getDatabase().eventDao().getNearlyEventByDate(date.date)
    }

    fun getEventById(id: Int): LiveData<Event?> {
        val liveData = MutableLiveData<Event?>()
        thread {
            val event = AppDatabase.getDatabase().eventDao().getEventById(id)
            liveData.postValue(event)
        }
        return liveData
    }

    suspend fun getBandEventByDate(date: IntDate, character1Id: Int) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getNearlyBandEventByDate(date.date, character1Id)
    }

   suspend fun getCharacterByMonth(month: Int) = withContext(Dispatchers.IO) {
        val formatMonth = if (month < 10) "0$month"
                          else month.toString()
       AppDatabase.getDatabase().characterDao().getCharacterByMonth(formatMonth)
    }

    suspend fun getCharacterById(id: Int) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().characterDao().getCharacterById(id)
    }

    fun getCharacterByName(name: String): LiveData<Character?> {
        val liveData = MutableLiveData<Character?>()
        thread {
            val character = AppDatabase.getDatabase().characterDao().getCharacterByName(name)
            liveData.postValue(character)
        }
        return liveData
    }

    fun getCharacterListFromInternet() = BangCalendarNetwork.getCharacterList()

    fun getEventListFromInternet() = BangCalendarNetwork.getEventList()

    fun login(request: LoginRequest) = liveData(Dispatchers.IO) {
        val result =
            if (request.phone == "1")
                Result.success(null) //登陆完成
            else {
                try {
                    val loginResponse = BangCalendarNetwork.login(request)
                    Result.success(loginResponse) //发起登录请求成功
                } catch (e: Exception) {
                    Result.failure(e) //登录请求失败
                }
            }
        emit(result)
    }

    fun getPhoneNum(): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            liveData.postValue(PreferenceDao.getPhoneNum())
        }
        return liveData
    }

    fun setPhoneNum(phone: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            PreferenceDao.setPhoneNum(phone)
            liveData.postValue(PreferenceDao.getPhoneNum())
        }
        return liveData
    }

    fun downloadUserPreference(request: LoginRequest) = liveData(Dispatchers.IO) {
        val result = try {
            val getResponse = BangCalendarNetwork.getUserPreference(request)
            Result.success(getResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    fun uploadUserPreference(userPreference: UserPreference) = liveData(Dispatchers.IO) {
        val result = try {
            val setResponse = BangCalendarNetwork.setUserPreference(userPreference)
            Result.success(setResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    fun getUserPreference(): LiveData<UserPreference> {
        val liveData = MutableLiveData<UserPreference>()
        thread {
            liveData.postValue(PreferenceDao.getUserPreference())
        }
        return liveData
    }

    fun setUserPreference(userPreference: UserPreference): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        thread {
            PreferenceDao.setUserPreference(userPreference)
            liveData.postValue(0)
        }
        return liveData
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

    suspend fun getEventPic(eventId: String) = suspendCoroutine<Drawable?> {
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

}