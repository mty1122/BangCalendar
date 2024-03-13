package com.mty.bangcalendar.logic.repository

import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.AppDatabase
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventRepository @Inject constructor() {

    private val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    suspend fun getEventByDate(date: IntDate) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getNearlyEventByDate(date.value)
    }

    suspend fun getEventById(id: Int) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getEventById(id)
    }

    suspend fun getBandEventByDate(date: IntDate, character1Id: Int) = withContext(Dispatchers.IO){
        if (character1Id < 1)
            null
        else
            AppDatabase.getDatabase().eventDao().getNearlyBandEventByDate(date.value, character1Id)
    }

    suspend fun getBandLastEventByDate(date: IntDate, character1Id: Int?) =
        withContext(Dispatchers.IO) {
            if (character1Id != null)
                AppDatabase.getDatabase().eventDao()
                    .getLastNearlyBandEventByDate(date.value, character1Id)
            else
                null
        }

    suspend fun getEventList() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase().eventDao().getEventList()
    }

    fun getEventPic(eventId: String) = callbackFlow {
        val uri = Uri.parse(
            ServiceCreator.BASE_URL + "event/banner_memorial_event$eventId.png")
        //设置回调
        val customTarget = object : CustomTarget<Drawable>() {
            private val target = this
            private var retryTimes: Long = 500

            override fun onResourceReady(resource: Drawable,
                                         transition: Transition<in Drawable>?) {
                trySend(resource)
                close()
            }
            override fun onLoadFailed(errorDrawable: Drawable?) {
                trySend(null)
                //加载失败后进行重试，一共重试三次
                if (retryTimes < 2001) {
                    launch {
                        delay(retryTimes)
                        retryTimes *= 2
                        Glide.with(BangCalendarApplication.context)
                            .load(uri).apply(glideOptions).into(target)
                    }
                } else {
                    close()
                }
            }
            override fun onLoadCleared(placeholder: Drawable?) {
                trySend(null)
                close()
            }
        }
        //获取图片
        Glide.with(BangCalendarApplication.context).load(uri).apply(glideOptions).into(customTarget)
        //关闭流后取消图片加载
        awaitClose {
            launch(Dispatchers.Main) {
                Glide.with(BangCalendarApplication.context).clear(customTarget)
            }
        }
    }

}