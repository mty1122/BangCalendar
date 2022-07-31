package com.mty.bangcalendar.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CharacterRefreshService : Service(){

    companion object {

        fun sendMessage(result: Int) {
            val context = BangCalendarApplication.context
            val intent = Intent("com.mty.bangcalendar.REFRESH_DATABASE_FINISH")
            intent.setPackage(context.packageName)
            intent.putExtra("result", result)
            context.sendBroadcast(intent)
        }

    }

    private val refreshBinder = RefreshBinder()

    class RefreshBinder : Binder() {

        fun refresh(progressBar: ProgressBar, textView: TextView) {
            val gson = Gson()
            val typeOf = object : TypeToken<List<Character>>() {}.type
            val characterReader = BufferedReader(InputStreamReader(
                Repository.getCharacterJSONStreamFromAssets()))
            val characterList = gson.fromJson<List<Character>>(characterReader, typeOf)
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                for (character in characterList) {
                    suspendCoroutine {
                        thread {
                            Repository.addCharacterToDatabase(character)
                            it.resume(Unit)
                        }
                    }
                    textView.text = StringBuilder().run {
                        append("载入角色：")
                        append(character.name)
                    }
                    progressBar.progress = 50 + character.id.toInt() / characterList.size * 50
                }
                sendMessage(SettingsActivity.REFRESH_CHARACTER_SUCCESS)
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