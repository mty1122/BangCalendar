package com.mty.bangcalendar.ui.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.service.AddCharacterService
import com.mty.bangcalendar.service.RefreshEventService

class MainViewModel : ViewModel() {

    private val isFirstStartLiveData = MutableLiveData<Any?>()

    val isFirstStart: LiveData<Boolean> = Transformations.switchMap(isFirstStartLiveData) {
        Repository.isFirstStart()
    }

    //调用该方法使isFirstStartLiveData的值改变从而调用Repository.isFirstStart()方法
    fun isFirstStart() {
        isFirstStartLiveData.value = isFirstStartLiveData.value
    }

    fun addCharacter(context: Context, isInit: Boolean) {
        val intent = Intent(context, AddCharacterService::class.java)
        intent.putExtra("isInit", isInit)
        context.startService(intent)
    }

    fun addEvent(context: Context, isInit: Boolean) {
        val intent = Intent(context, RefreshEventService::class.java)
        intent.putExtra("isInit", isInit)
        intent.putExtra("isInsert", true)
        context.startService(intent)
    }

}