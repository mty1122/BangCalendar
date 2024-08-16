package com.mty.bangcalendar.ui.main.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.EventCardBinding
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.main.state.EventCardUiState
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.ThemeUtil
import com.mty.bangcalendar.util.startActivity
import com.mty.bangcalendar.util.startCharacterListActivity
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class EventCardView @Inject constructor(
    @ActivityContext private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    //记录活动卡片的可见性
    private var isEventCardVisible = true
    //记录活动图片获取Job
    private var eventPictureJob: Job? = null

    fun eventCardInit(
        binding: EventCardBinding,
        eventCardUiState: EventCardUiState,
        currentDate: IntDate,
    ) {
        //活动进度条初始化
        binding.eventProgress.progressColor = ThemeUtil.getThemeColor(context)
        binding.eventProgress.textColor = ThemeUtil.getThemeColor(context)

        //活动卡片内容初始化
        val event = eventCardUiState.event
        if (event == null || currentDate - IntDate(event.startDate) >= 13) {
            isEventCardVisible = false
            binding.eventCardItem.alpha = 0f
        } else {
            isEventCardVisible = true
            refreshEventComponent(event,
                eventCardUiState.eventPicture!!, eventCardUiState.progress!!, binding)
        }
    }

    fun handleUiState(
        currentDate: IntDate,
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
                refreshEventComponent(eventCardUiState.event,
                    eventCardUiState.eventPicture!!, eventCardUiState.progress!!, binding)
                //启动显示动画
                runEventCardAnim(binding, 1f)
                //不同活动之间移动，刷新活动
            } else if (!EventUtil.isSameEvent(binding.eventType.text.toString(),
                    eventCardUiState.event.id.toInt())) {
                refreshEventComponent(eventCardUiState.event,
                    eventCardUiState.eventPicture!!, eventCardUiState.progress!!, binding)
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

    private fun refreshEventComponent(
        event: Event,
        eventPicture: Flow<Drawable?>,
        eventProgress: Int,
        binding: EventCardBinding
    ) {
        LogUtil.d(this, "刷新活动组件")
        //刷新活动状态
        refreshEventStatus(eventProgress, binding)
        //刷新活动类型
        binding.eventType.text = StringBuilder().run {
            append("活动")
            append(event.id)
            append(" ")
            append(EventUtil.matchType(event.type))
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
        if (event.character4 != null) {
            binding.char4.apply {
                visibility = View.VISIBLE
                Glide.with(context).load(EventUtil.matchCharacter(event.character4))
                    .into(this)
                setOnClickListener {
                    context.startCharacterListActivity(event.character4!!)
                }
            }
        } else {
            binding.char4.visibility = View.INVISIBLE
        }
        if (event.character5 != null) {
            binding.char5.apply {
                visibility = View.VISIBLE
                Glide.with(context).load(EventUtil.matchCharacter(event.character5))
                    .into(this)
                setOnClickListener {
                    context.startCharacterListActivity(event.character5!!)
                }
            }
        } else {
            binding.char5.visibility = View.INVISIBLE
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
        eventPictureJob?.cancel()
        eventPictureJob = lifecycleOwner.lifecycleScope.launch {
            eventPicture.collect {
                binding.eventBackground.background = it
            }
        }
        binding.eventButton.setOnClickListener {
            context.startActivity<EventListActivity>("current_id" to event.id.toInt())
        }
    }

    private fun refreshEventStatus(
        eventProgress: Int,
        binding: EventCardBinding
    ) {
        binding.eventProgress.progress = eventProgress
        when {
            eventProgress <= 0 -> binding.eventProgressName.setText(R.string.prepare)
            eventProgress >= 100 -> binding.eventProgressName.setText(R.string.finish)
            else -> binding.eventProgressName.setText(R.string.ing)
        }
    }

}