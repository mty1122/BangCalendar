package com.mty.bangcalendar.logic.repository

import com.mty.bangcalendar.logic.dao.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CharacterRepository @Inject constructor() {

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

    suspend fun getCharacterList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().characterDao().getCharacterList()
    }

}