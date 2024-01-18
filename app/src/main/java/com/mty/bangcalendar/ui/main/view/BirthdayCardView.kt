package com.mty.bangcalendar.ui.main.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.CharacterUtil
import com.mty.bangcalendar.util.GenericUtil
import com.mty.bangcalendar.util.log
import com.mty.bangcalendar.util.startCharacterListActivity

class BirthdayCardView {

    //记录滑动手势的起始点，用于折叠卡片
    private var touchEventStartY = 0f

    //处理生日卡片折叠/展开动画时的手势
    fun handleMainViewTouchEvent(
        event: MotionEvent,
        binding: ActivityMainBinding,
        getBirthdayCardUiState: () -> Int,
        getBirthdayCardVisibility: () -> Boolean,
        setBirthdayCardVisible: (Boolean) -> Unit
    ) {
        //生日卡片不显示时不处理
        if (getBirthdayCardUiState() < 1)
            return
        //处理滑动手势
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchEventStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - touchEventStartY
                //向下滑动，触发展开动画
                if (deltaY > 0 && !getBirthdayCardVisibility()) {
                    setBirthdayCardVisible(true)
                    runBirthdayCardAnim(binding, true)
                    //向上滑动，触发折叠动画
                } else if (deltaY < 0 && getBirthdayCardVisibility()) {
                    setBirthdayCardVisible(false)
                    runBirthdayCardAnim(binding, false)
                }
            }
        }
    }

    //刷新生日卡片
    fun refreshBirthdayCard(context: Context, id: Int, binding: ActivityMainBinding) {
        if (id == 12 || id == 17) {
            Glide.with(context).load(CharacterUtil.matchCharacter(12))
                .into(binding.birCard.birChar1)
            Glide.with(context).load(CharacterUtil.matchCharacter(17))
                .into(binding.birCard.birChar2)
            binding.birCard.birChar1.setOnClickListener {
                context.startCharacterListActivity(12)
            }
            binding.birCard.birChar2.setOnClickListener {
                context.startCharacterListActivity(17)
            }
            binding.birCard.birChar2.visibility = View.VISIBLE
        } else {
            binding.birCard.birChar2.visibility = View.GONE
            Glide.with(context).load(CharacterUtil.matchCharacter(id))
                .into(binding.birCard.birChar1)
            binding.birCard.birChar1.setOnClickListener {
                context.startCharacterListActivity(id)
            }
            binding.birCard.birChar2.setOnClickListener {
                context.startCharacterListActivity(id)
            }
        }
    }

    fun runBirthdayCardAnim(binding: ActivityMainBinding, isInsert: Boolean) {
        val mainLinearLayout = binding.mainView
        val birCardIndex = mainLinearLayout.indexOfChild(binding.birCardParent)
        val animDuration = AnimUtil.getAnimPreference().toLong()
        //获取生日卡片的高度
        val cardHeight = binding.birCard.cardView.height.toFloat()
        //这里需要多往上移动生日卡片和下方卡片的间距（margin）
        val endPosition = if (isInsert) 0f else -cardHeight - GenericUtil.dpToPx(10)
        //创建垂直位移动画
        val translationYAnimator = ObjectAnimator.ofFloat(binding.birCard.cardView,
            "translationY", endPosition)
        translationYAnimator.duration = animDuration // 设置垂直位移动画时长
        translationYAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                //处理下方卡片
                for (i in birCardIndex + 1 until mainLinearLayout.childCount) {
                    val cardBelow = mainLinearLayout.getChildAt(i)
                    val moveAnimator = ObjectAnimator.ofFloat(cardBelow,
                        "translationY", endPosition
                    )
                    moveAnimator.duration = animDuration // 设置下移动画时长
                    moveAnimator.start()
                }
            }
        })
        translationYAnimator.start()
        log(this, "生日卡片动画启动")
    }

}