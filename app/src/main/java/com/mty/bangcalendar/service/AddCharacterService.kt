package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.util.LogUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class AddCharacterService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        thread {
            val isInit = intent.getBooleanExtra("isInit", true)
            val gson = Gson()
            val typeOf = object : TypeToken<List<Character>>() {}.type
            if (isInit) {
                val characterReader = BufferedReader(InputStreamReader(
                    Repository.getCharacterJSONStreamFromAssets()))
                val characterList = gson.fromJson<List<Character>>(characterReader, typeOf)
                for (character in characterList) {
                    Repository.addCharacterToDatabase(character)
                    LogUtil.d("AppInit", "向数据库加入角色：$character")
                }
            }
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

}