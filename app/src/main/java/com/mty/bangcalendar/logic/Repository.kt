package com.mty.bangcalendar.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
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

    fun isFirstStart(): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        thread {
            liveData.postValue(PreferenceDao.isFirstStart())
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

    fun getCharacterListFromInternet() = BangCalendarNetwork.getCharacterList()

    fun getEventListFromInternet() = BangCalendarNetwork.getEventList()

}