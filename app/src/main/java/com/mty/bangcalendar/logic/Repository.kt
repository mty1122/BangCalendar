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

    fun getEventByDate(date: Int): LiveData<Event> {
        val liveData = MutableLiveData<Event>()
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

}