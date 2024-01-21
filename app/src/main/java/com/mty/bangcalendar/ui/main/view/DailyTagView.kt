package com.mty.bangcalendar.ui.main.view

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.databinding.DailytagCardBinding
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.main.state.DailyTagUiState
import com.mty.bangcalendar.ui.main.state.shouldBandItemVisible
import com.mty.bangcalendar.ui.main.state.shouldCharacterItemVisible
import com.mty.bangcalendar.ui.main.state.shouldVisible
import com.mty.bangcalendar.util.CharacterUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.startActivity
import com.mty.bangcalendar.util.startCharacterListActivity
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class DailyTagView @Inject constructor(@ActivityContext val context: Context) {

    //刷新dailyTag
    fun refreshDailyTag(
        binding: DailytagCardBinding,
        uiState: DailyTagUiState,
        jumpDate: (IntDate) -> Unit
    ) {
        if (!uiState.shouldVisible()) {
            binding.cardView.visibility = View.GONE
            return
        }
        //刷新标题
        binding.dailytagTitle.text = StringBuilder().apply {
            append("${BangCalendarApplication.systemDate.getTimeName()}好")
            if (uiState.userName != "")
                append("，${uiState.userName}")
        }
        //刷新角色订阅
        uiState.preferenceCharacter?.let { character->
            binding.dailytagCardBirthday.run {
                //角色头像和名字
                Glide.with(context)
                    .load(EventUtil.matchCharacter(character.id.toInt())).into(charImage)
                charImage.setOnClickListener {
                    context.startCharacterListActivity(character.id.toInt())
                }
                Glide.with(context)
                    .load(CharacterUtil.matchCharacter(character.id.toInt())).into(charNameImage)
                charNameImage.setOnClickListener {
                    context.startCharacterListActivity(character.id.toInt())
                }
                //倒数日
                val birthdayAway = CharacterUtil.birthdayAway(character.birthday,
                    BangCalendarApplication.systemDate
                )
                birthdayCountdown.text = birthdayAway.toString()
                birthdayCountdown.setOnClickListener {
                    val target = CharacterUtil.getNextBirthdayDate(character.birthday,
                            BangCalendarApplication.systemDate)
                    jumpDate(target)
                }
                //更新进度条
                birBar.setProgressCompat(
                    ((365 - birthdayAway) / 365.0 * 100).toInt(), true)
                birthdayView.visibility = View.VISIBLE
            }
        }
        if (!uiState.shouldCharacterItemVisible())
            binding.dailytagCardBirthday.birthdayView.visibility = View.GONE
        //刷新活动订阅
        uiState.preferenceBandNextEvent?.let { event->
            binding.dailytagCardEvent.run {
                //刷新活动属性
                Glide.with(context).load(EventUtil.matchAttrs(event.attrs))
                    .into(eventAttrs)
                //刷新乐队图片
                Glide.with(context).load(EventUtil.getBandPic(event))
                    .into(bandImage)
                bandImage.setOnClickListener {
                    context.startActivity<EventListActivity>(
                        "current_id" to event.id.toInt(),
                        "band_id" to EventUtil.getBand(event).id
                    )
                }
                //倒数日
                val eventAway = (IntDate(event.startDate) -
                        BangCalendarApplication.systemDate.toDate())
                eventCountdown.text = eventAway.toString()
                eventCountdown.setOnClickListener {
                    jumpDate(IntDate(event.startDate))
                }
                //新乐队可能没有往期活动
                val lastDate = uiState.preferenceBandLatestEvent?.startDate
                    ?: BangCalendarApplication.systemDate.toDate().value
                val interval = IntDate(event.startDate) - IntDate(lastDate)
                //更新进度条
                eventBar.setProgressCompat(
                    ((interval - eventAway) / interval.toDouble() * 100).toInt(), true)
                eventView.visibility = View.VISIBLE
            }
        }
        if (!uiState.shouldBandItemVisible())
            binding.dailytagCardEvent.eventView.visibility = View.GONE
        //用户偏好存在时，启动dailyTag
        binding.cardView.visibility = View.VISIBLE
    }

}