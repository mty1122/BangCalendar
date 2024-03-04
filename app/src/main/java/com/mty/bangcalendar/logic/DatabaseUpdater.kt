package com.mty.bangcalendar.logic

import androidx.room.withTransaction
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.network.BangCalendarNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

object DatabaseUpdater {

    private val internalScope = CoroutineScope(Dispatchers.IO)

    enum class DatabaseUpdateState {
        PREPARE, SUCCESS_EVENT, SUCCESS_CHARACTER, ERROR
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun initDatabase(onItemAdded: suspend (Int, String) -> Unit) = internalScope.async {
        val eventReader = BangCalendarApplication.context.assets.open("event.json")
        val eventList = Json.decodeFromStream<List<Event>>(eventReader)
        val characterReader = BangCalendarApplication.context.assets.open("character.json")
        val characterList = Json.decodeFromStream<List<Character>>(characterReader)

        val totalSize = eventList.size + characterList.size
        var finishedSize = 0

        AppDatabase.getDatabase().withTransaction {
            for (event in eventList) {
                AppDatabase.getDatabase().eventDao().insertEvent(event)
                finishedSize++
                val progress = finishedSize * 100 / totalSize
                val details = "载入活动：${event.id}"
                onItemAdded(progress, details)
            }
            for (character in characterList) {
                AppDatabase.getDatabase().characterDao().insertCharacter(character)
                finishedSize++
                val progress = finishedSize * 100 / totalSize
                val details = "载入角色：${character.name}"
                onItemAdded(progress, details)
            }
        }
    }

    fun updateDatabase(): StateFlow<DatabaseUpdateState> {
        val updateStateFlow = MutableStateFlow(DatabaseUpdateState.PREPARE)
        internalScope.launch {
            val eventList: List<Event>
            val characterList: List<Character>
            try {
                eventList = BangCalendarNetwork.getEventList()
                characterList = BangCalendarNetwork.getCharacterList()
            } catch (e: Exception) {
                e.printStackTrace()
                updateStateFlow.value = DatabaseUpdateState.ERROR
                return@launch
            }

            AppDatabase.getDatabase().eventDao().insertAll(eventList)
            updateStateFlow.value = DatabaseUpdateState.SUCCESS_EVENT
            AppDatabase.getDatabase().characterDao().insertAll(characterList)
            updateStateFlow.value = DatabaseUpdateState.SUCCESS_CHARACTER

            PreferenceDao.setLastRefreshDate(BangCalendarApplication.systemDate.toDate().value)
        }
        return updateStateFlow.asStateFlow()
    }

}