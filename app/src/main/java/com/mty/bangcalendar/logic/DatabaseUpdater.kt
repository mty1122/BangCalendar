package com.mty.bangcalendar.logic

import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

object DatabaseUpdater {

    private val internalScope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalSerializationApi::class)
    fun initDatabase(onItemAdded: (Int, String) -> Unit) = internalScope.async {
        val eventReader = BangCalendarApplication.context.assets.open("event.json")
        val eventList = Json.decodeFromStream<List<Event>>(eventReader)
        val characterReader = BangCalendarApplication.context.assets.open("character.json")
        val characterList = Json.decodeFromStream<List<Character>>(characterReader)

        val totalSize = eventList.size + characterList.size
        var finishedSize = 0

        Repository.withDatabaseTransaction {
            for (event in eventList) {
                AppDatabase.getDatabase().eventDao().insertEvent(event)
                finishedSize++
                val progress = finishedSize / totalSize
                val details = "载入活动：${event.id}"
                withContext(Dispatchers.Main) {
                    onItemAdded(progress, details)
                }
            }
            for (character in characterList) {
                AppDatabase.getDatabase().characterDao().insertCharacter(character)
                finishedSize++
                val progress = finishedSize / totalSize
                val details = "载入角色：${character.name}"
                withContext(Dispatchers.Main) {
                    onItemAdded(progress, details)
                }
            }
        }
    }

}