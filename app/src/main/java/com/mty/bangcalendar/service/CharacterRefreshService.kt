package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.service.EventRefreshService.Companion.sendMessage
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.LogUtil
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class CharacterRefreshService : Service(){

    private val refreshBinder = RefreshBinder()

    class RefreshBinder : Binder() {

        @OptIn(ExperimentalSerializationApi::class)
        fun refresh(onItemAddFinished: (Int, String) -> Unit) {
            val characterReader = Repository.getCharacterJSONStreamFromAssets()
            val characterList = Json.decodeFromStream<List<Character>>(characterReader)
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                for (character in characterList) {
                    withContext(Dispatchers.IO) {
                        Repository.addCharacterToDatabase(character)
                    }
                    val progress = 50 + character.id.toInt() * 50 / characterList.size
                    val details = "载入角色：${character.name}"
                    onItemAddFinished(progress, details)
                }
                coroutineScope.cancel()
            }
        }

    }

    override fun onBind(intent: Intent): IBinder = refreshBinder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Repository.getCharacterListFromInternet().enqueue(object :
            Callback<List<Character>> {
            override fun onResponse(call: Call<List<Character>>,
                                    response: Response<List<Character>>) {
                val characterList = response.body()
                if (characterList != null) {
                    addCharacterToDatabase(characterList)
                } else {
                    sendMessage(SettingsActivity.REFRESH_CHARACTER_FAILURE)
                    stopSelf()
                }
            }

            override fun onFailure(call: Call<List<Character>>, t: Throwable) {
                t.printStackTrace()
                sendMessage(SettingsActivity.REFRESH_CHARACTER_FAILURE)
                stopSelf()
            }
        })
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addCharacterToDatabase(characterList: List<Character>) {
        thread {
            for (character in characterList) {
                Repository.addCharacterToDatabase(character)
                LogUtil.d("Database", "向数据库加入角色：$character")
            }
            sendMessage(SettingsActivity.REFRESH_CHARACTER_SUCCESS)
            stopSelf()
        }
    }

}