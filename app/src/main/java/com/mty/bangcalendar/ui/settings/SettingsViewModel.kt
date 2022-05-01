package com.mty.bangcalendar.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.service.RefreshCharacterService
import com.mty.bangcalendar.service.RefreshEventService

class SettingsViewModel : ViewModel() {

    //更新数据库数据
    fun addCharacter(context: Context) {
        val intent = Intent(context, RefreshCharacterService::class.java)
        intent.putExtra("isInit", false)
        context.startService(intent)
    }

    fun addEvent(context: Context) {
        val intent = Intent(context, RefreshEventService::class.java)
        intent.putExtra("isInit", false)
        context.startService(intent)
    }

}