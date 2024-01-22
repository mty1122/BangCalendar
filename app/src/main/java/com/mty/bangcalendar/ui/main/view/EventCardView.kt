package com.mty.bangcalendar.ui.main.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.EventCardBinding
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.main.state.EventCardUiState
import com.mty.bangcalendar.ui.main.state.MainUiState
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.ThemeUtil
import com.mty.bangcalendar.util.log
import com.mty.bangcalendar.util.startActivity
import com.mty.bangcalendar.util.startCharacterListActivity
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventCardView @Inject constructor(@ActivityContext val context: Context) {

    //记录活动卡片的可见性
    private var isEventCardVisible = true

    suspend fun eventCardInit(
        binding: EventCardBinding,
        mainUiState: MainUiState,
        event: Event?,
        currentDate: IntDate,
        eventPicture: Flow<Drawable?>?
    ) {
        //活动进度条初始化
        binding.eventProgress.progressColor = ThemeUtil.getThemeColor(context)
        binding.eventProgress.textColor = ThemeUtil.getThemeColor(context)

        //活动卡片内容初始化
        if (event == null || currentDate - IntDate(event.startDate) >= 13) {
            isEventCardVisible = false
            binding.eventCardItem.alpha = 0f
        } else {
            isEventCardVisible = true
            refreshEventComponent(mainUiState, event, eventPicture!!, binding)
        }
    }

    suspend fun handleUiState(
        currentDate: IntDate,
        mainUiState: MainUiState,
        eventCardUiState: EventCardUiState,
        binding: EventCardBinding
    ) {
        //活动小于第一期或者大于最后一期的情况
        if (eventCardUiState.event == null ||
            currentDate - IntDate(eventCardUiState.event.startDate) >= 13) {
            if (isEventCardVisible) {
                isEventCardVisible = false
                //启动隐藏动画
                runEventCardAnim(binding, 0f)
                //取消注册监听器
                cancelListener(binding)
            }
            //活动合法的情况
        } else {
            LogUtil.d("Event", "Event id is ${eventCardUiState.event.id}")
            //不可见时，刷新活动
            if (!isEventCardVisible) {
                isEventCardVisible = true
                refreshEventComponent(mainUiState, eventCardUiState.event,
                    eventCardUiState.eventPicture!!, binding)
                //启动显示动画
                runEventCardAnim(binding, 1f)
                //不同活动之间移动，刷新活动
            } else if (!EventUtil.isSameEvent(binding.eventType.text.toString(),
                    eventCardUiState.event.id.toInt())) {
                refreshEventComponent(mainUiState, eventCardUiState.event,
                    eventCardUiState.eventPicture!!, binding)
            }
        }
    }

    private fun runEventCardAnim(binding: EventCardBinding, endAlpha: Float) {
        binding.eventCardItem.run {
            if (endAlpha != alpha)
                ObjectAnimator.ofFloat(this, "alpha", endAlpha)
                    .setDuration(AnimUtil.getAnimPreference().toLong())
                    .start()

        }
    }

    private fun cancelListener(binding: EventCardBinding) {
        binding.char1.setOnClickListener(null)
        binding.char2.setOnClickListener(null)
        binding.char3.setOnClickListener(null)
        binding.char4.setOnClickListener(null)
        binding.char5.setOnClickListener(null)
        binding.eventBand.setOnClickListener(null)
        binding.eventButton.setOnClickListener(null)
    }

    private suspend fun refreshEventComponent(
        mainUiState: MainUiState,
        event: Event,
        eventPicture: Flow<Drawable?>,
        binding: EventCardBinding
    ) {
        LogUtil.d(this, "刷新活动组件")
        //刷新活动状态
        refreshEventStatus(event, binding, mainUiState)
        //刷新活动类型
        binding.eventType.text = StringBuilder().run {
            append("活动")
            append(event.id)
            append(" ")
            append(EventUtil.matchType(event.type))
            toString()
        }
        //刷新活动角色
        Glide.with(context).load(EventUtil.matchCharacter(event.character1))
            .into(binding.char1)
        binding.char1.setOnClickListener {
            context.startCharacterListActivity(event.character1)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character2))
            .into(binding.char2)
        binding.char2.setOnClickListener {
            context.startCharacterListActivity(event.character2)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character3))
            .into(binding.char3)
        binding.char3.setOnClickListener {
            context.startCharacterListActivity(event.character3)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character4))
            .into(binding.char4)
        event.character4?.let { character4 ->
            binding.char4.setOnClickListener {
                context.startCharacterListActivity(character4)
            }
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character5))
            .into(binding.char5)
        event.character5?.let { character5 ->
            binding.char5.setOnClickListener {
                context.startCharacterListActivity(character5)
            }
        }
        //刷新活动属性
        Glide.with(context).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.eventAttrs)
        //刷新乐队图片
        Glide.with(context).load(EventUtil.getBandPic(event))
            .into(binding.eventBand)
        binding.eventBand.setOnClickListener {
            context.startActivity<EventListActivity>(
                "current_id" to event.id.toInt(),
                "band_id" to EventUtil.getBand(event).id
            )
        }
        //刷新活动图片
        eventPicture.collect{
            it?.let {
                binding.eventBackground.background = it
            }
        }
        binding.eventButton.setOnClickListener {
            context.startActivity<EventListActivity>("current_id" to event.id.toInt())
        }
    }

    private fun refreshEventStatus(
        event: Event,
        binding: EventCardBinding,
        mainUiState: MainUiState
    ) {
        val todayEventId = mainUiState.todayEvent?.id
        val eventId = event.id
        todayEventId?.let {
            log(this, "刷新活动状态")
            when {
                eventId < todayEventId -> {
                    binding.eventProgressName.setText(R.string.finish)
                    binding.eventProgress.progress = 100
                }
                eventId == todayEventId -> {
                    val systemTime = systemDate.getTimeInMillis()
                    val eventStartTime = mainUiState.eventStartTime
                    val eventEndTime = mainUiState.eventEndTime
                    when {
                        systemTime < eventStartTime -> {
                            binding.eventProgressName.setText(R.string.prepare)
                            binding.eventProgress.progress = 0
                        }
                        systemTime >= eventEndTime -> {
                            binding.eventProgressName.setText(R.string.finish)
                            binding.eventProgress.progress = 100
                        }
                        else -> {
                            binding.eventProgressName.setText(R.string.ing)
                            binding.eventProgress.progress = EventUtil
                                .getEventProgress(systemTime, eventStartTime)
                        }
                    }
                }
                else -> {
                    binding.eventProgressName.setText(R.string.prepare)
                    binding.eventProgress.progress = 0
                }
            }
        }
    }

}