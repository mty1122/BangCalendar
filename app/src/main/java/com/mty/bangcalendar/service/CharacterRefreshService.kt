package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class CharacterRefreshService : Service(){

    override fun onBind(intent: Intent): IBinder? {
        //暂时无需与activity绑定，待完善
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            val isInit = intent.getBooleanExtra("isInit", true)
            if (isInit) {
                val gson = Gson()
                val typeOf = object : TypeToken<List<Character>>() {}.type
                val characterReader = BufferedReader(InputStreamReader(
                    Repository.getCharacterJSONStreamFromAssets()))
                val characterList = gson.fromJson<List<Character>>(characterReader, typeOf)
                addCharacterToDatabase(characterList)
            } else {
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
            }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendMessage(result: Int) {
        val intent = Intent("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
        intent.setPackage(packageName)
        intent.putExtra("result", result)
        sendBroadcast(intent)
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