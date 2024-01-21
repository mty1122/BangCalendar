package com.mty.bangcalendar.ui.main.view

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.main.adapter.CalendarViewAdapter
import com.mty.bangcalendar.ui.main.adapter.CalendarViewPagerAdapter
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.CharacterUtil
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarView @Inject constructor() {

    var calendarCurrentPosition = 1 //当前view在viewPager中的位置

    @SuppressLint("NotifyDataSetChanged")
    fun jumpDate(
        viewPager: ViewPager,
        target: CalendarUtil,
        lifecycleScope: LifecycleCoroutineScope,
        onDateChange: (IntDate) -> Unit,
        fetchCharacterByMonth: suspend (Int) -> List<Character>
    ) {
        //刷新当前日期
        onDateChange(target.toDate())
        val viewPagerAdapter = viewPager.adapter as CalendarViewPagerAdapter
        val scrollViewList = viewPagerAdapter.views
        var lastPosition = 0
        var relativeMonth = -1
        //初始化view集合中view的日期
        lifecycleScope.launch {
            for (scrollView in scrollViewList) {
                scrollView.lastPosition = lastPosition //设置上次位置
                val viewAdapter = (scrollView.view as RecyclerView)
                    .adapter as CalendarViewAdapter
                val calendarUtil = viewAdapter.calendarUtil
                calendarUtil.year = target.year
                calendarUtil.month = target.month + relativeMonth
                //获取初始数据
                val dateList = calendarUtil.getDateList()
                val characterList = fetchCharacterByMonth(calendarUtil.month)
                val birthdayMap = CharacterUtil.characterListToBirthdayMap(characterList)
                val calendarItemUiState = viewAdapter.uiState.copy(
                    isVisible = lastPosition == 1,
                    dateList = dateList,
                    birthdayMap = birthdayMap
                )
                viewAdapter.uiState = calendarItemUiState
                //刷新日历
                viewAdapter.notifyDataSetChanged()
                //下一个日历界面
                lastPosition++
                relativeMonth++
            }
            //初始化viewPager的当前item
            viewPager.currentItem = 1
        }
    }

}