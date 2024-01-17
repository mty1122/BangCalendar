package com.mty.bangcalendar.ui.main.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.log
import com.mty.bangcalendar.util.startActivity
import com.mty.bangcalendar.util.startCharacterListActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EventCardView {

    fun runEventCardAnim(mainBinding: ActivityMainBinding, endAlpha: Float) {
        mainBinding.eventCard.eventCardItem.run {
            if (endAlpha != alpha)
                ObjectAnimator.ofFloat(this, "alpha", endAlpha)
                    .setDuration(AnimUtil.getAnimPreference().toLong())
                    .start()

        }
    }

    fun cancelListener(mainBinding: ActivityMainBinding) {
        mainBinding.eventCard.char1.setOnClickListener(null)
        mainBinding.eventCard.char2.setOnClickListener(null)
        mainBinding.eventCard.char3.setOnClickListener(null)
        mainBinding.eventCard.char4.setOnClickListener(null)
        mainBinding.eventCard.char5.setOnClickListener(null)
        mainBinding.eventCard.eventBand.setOnClickListener(null)
        mainBinding.eventCard.eventButton.setOnClickListener(null)
    }

    fun refreshEventComponent(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        event: Event,
        eventPicture: Flow<Drawable?>,
        binding: ActivityMainBinding
    ) {
        LogUtil.d(this, "刷新活动组件")
        //刷新活动状态
        refreshEventStatus(event, binding)
        //刷新活动类型
        binding.eventCard.eventType.text = StringBuilder().run {
            append("活动")
            append(event.id)
            append(" ")
            append(EventUtil.matchType(event.type))
            toString()
        }
        //刷新活动角色
        Glide.with(context).load(EventUtil.matchCharacter(event.character1))
            .into(binding.eventCard.char1)
        binding.eventCard.char1.setOnClickListener {
            context.startCharacterListActivity(event.character1)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character2))
            .into(binding.eventCard.char2)
        binding.eventCard.char2.setOnClickListener {
            context.startCharacterListActivity(event.character2)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character3))
            .into(binding.eventCard.char3)
        binding.eventCard.char3.setOnClickListener {
            context.startCharacterListActivity(event.character3)
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character4))
            .into(binding.eventCard.char4)
        event.character4?.let { character4 ->
            binding.eventCard.char4.setOnClickListener {
                context.startCharacterListActivity(character4)
            }
        }
        Glide.with(context).load(EventUtil.matchCharacter(event.character5))
            .into(binding.eventCard.char5)
        event.character5?.let { character5 ->
            binding.eventCard.char5.setOnClickListener {
                context.startCharacterListActivity(character5)
            }
        }
        //刷新活动属性
        Glide.with(context).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.eventCard.eventAttrs)
        //刷新乐队图片
        Glide.with(context).load(EventUtil.getBandPic(event))
            .into(binding.eventCard.eventBand)
        binding.eventCard.eventBand.setOnClickListener {
            context.startActivity<EventListActivity>(
                "current_id" to event.id.toInt(),
                "band_id" to EventUtil.getBand(event).id
            )
        }
        //刷新活动图片
        lifecycleScope.launch {
            eventPicture.collect{
                it?.let {
                    binding.eventCard.eventBackground.background = it
                }
            }
        }
        binding.eventCard.eventButton.setOnClickListener {
            context.startActivity<EventListActivity>("current_id" to event.id.toInt())
        }
    }

    private fun refreshEventStatus(event: Event, binding: ActivityMainBinding) {
        val todayEventId = viewModel.todayEvent?.id
        val eventId = event.id
        todayEventId?.let {
            log(this, "刷新活动状态")
            when {
                eventId < todayEventId -> {
                    binding.eventCard.eventProgressName.setText(R.string.finish)
                    binding.eventCard.eventProgress.progress = 100
                }
                eventId == todayEventId -> {
                    val systemTime = systemDate.getTimeInMillis()
                    val eventStartTime = viewModel.eventStartTime
                    val eventEndTime = viewModel.eventEndTime
                    if (eventStartTime != null && eventEndTime != null) {
                        when {
                            systemTime < eventStartTime -> {
                                binding.eventCard.eventProgressName.setText(R.string.prepare)
                                binding.eventCard.eventProgress.progress = 0
                            }
                            systemTime >= eventEndTime -> {
                                binding.eventCard.eventProgressName.setText(R.string.finish)
                                binding.eventCard.eventProgress.progress = 100
                            }
                            else -> {
                                binding.eventCard.eventProgressName.setText(R.string.ing)
                                binding.eventCard.eventProgress.progress = EventUtil
                                    .getEventProgress(systemTime, eventStartTime)
                            }
                        }
                    }
                }
                else -> {
                    binding.eventCard.eventProgressName.setText(R.string.prepare)
                    binding.eventCard.eventProgress.progress = 0
                }
            }
        }
    }

}