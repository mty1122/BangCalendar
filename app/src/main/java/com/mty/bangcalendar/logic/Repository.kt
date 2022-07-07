package com.mty.bangcalendar.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.GuideInitData
import com.mty.bangcalendar.logic.model.UserPreference
import com.mty.bangcalendar.logic.network.BangCalendarNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import kotlin.concurrent.thread

object Repository {

    fun getCharacterJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("Character.json")

    fun getEventJSONStreamFromAssets() =
        BangCalendarApplication.context.assets.open("Event.json")

    fun addCharacterToDatabase(character: Character) {
        AppDatabase.getDatabase().characterDao().insertCharacter(character)
    }

    fun addEventToDatabase(event: Event) {
        AppDatabase.getDatabase().eventDao().insertEvent(event)
    }

    fun getGuideInitData(): LiveData<GuideInitData> {
        val liveData = MutableLiveData<GuideInitData>()
        thread {
            val isFirstStart = PreferenceDao.isFirstStart()
            val theme = PreferenceDao.getTheme()
            liveData.postValue(GuideInitData(isFirstStart, theme))
        }
        return liveData
    }

    fun getAdditionalTip(): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            liveData.postValue(PreferenceDao.getAdditionalTip())
        }
        return liveData
    }

    fun setAdditionalTip(additionalTip: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            PreferenceDao.setAdditionalTip(additionalTip)
            liveData.postValue(PreferenceDao.getAdditionalTip())
        }
        return liveData
    }

    fun getUserName(): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            liveData.postValue(PreferenceDao.getUserName())
        }
        return liveData
    }

    fun getPreferenceBand(): LiveData<String> {
        val liveData = MutableLiveData<String>()
        thread {
            liveData.postValue(PreferenceDao.getPreferenceBand())
        }
        return liveData
    }

    fun getPreferenceCharacter(): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        thread {
            liveData.postValue(PreferenceDao.getPreferenceCharacter())
        }
        return liveData
    }

    fun getEventByDate(date: Int): LiveData<Event?> {
        val liveData = MutableLiveData<Event?>()
        thread {
            val event = AppDatabase.getDatabase().eventDao().getNearlyEventByDate(date)
            liveData.postValue(event)
        }
        return liveData
    }

    fun getEventById(id: Int): LiveData<Event?> {
        val liveData = MutableLiveData<Event?>()
        thread {
            val event = AppDatabase.getDatabase().eventDao().getEventById(id)
            liveData.postValue(event)
        }
        return liveData
    }

    fun getBandEventByDate(date: Int, character1Id: Int): LiveData<Event?> {
        val liveData = MutableLiveData<Event?>()
        thread {
            val event = AppDatabase.getDatabase().eventDao()
                .getNearlyBandEventByDate(date, character1Id)
            liveData.postValue(event)
        }
        return liveData
    }

    fun getEventPicture(eventId: String) = liveData(Dispatchers.IO) {
        val result = try {
            val pictureResponse = BangCalendarNetwork.getEventPicture(eventId)
            Result.success(pictureResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    fun getCharacterByMonth(month: Int): LiveData<List<Character>> {
        val formatMonth = if (month < 10) "0$month"
                          else month.toString()
        val liveData = MutableLiveData<List<Character>>()
        thread {
            val characterList = AppDatabase.getDatabase().characterDao()
                .getCharacterByMonth(formatMonth)
            liveData.postValue(characterList)
        }
        return liveData
    }

    fun getCharacterById(id: Int): LiveData<Character> {
        val liveData = MutableLiveData<Character>()
        thread {
            val character = AppDatabase.getDatabase().characterDao().getCharacterById(id)
            liveData.postValue(character)
        }
        return liveData
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

    fun login(phone: String) = liveData(Dispatchers.IO) {
        val result =
            if (phone == "1")
                Result.success(null) //登陆完成
            else {
                try {
                    val loginResponse = BangCalendarNetwork.login(phone)
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

    fun downloadUserPreference(phone: String) = liveData(Dispatchers.IO) {
        val result = try {
            val getResponse = BangCalendarNetwork.getUserPreference(phone)
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

}