package com.mty.bangcalendar.ui.main.view

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.model.MainViewInitData
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.main.MainViewModel
import com.mty.bangcalendar.ui.main.adapter.CalendarViewAdapter
import com.mty.bangcalendar.ui.main.adapter.CalendarViewPagerAdapter
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.CharacterUtil
import kotlinx.coroutines.flow.Flow

class MainViewInitializer(
    private val mainActivity: MainActivity,
    private val binding: ActivityMainBinding,
    private val viewModel: MainViewModel,
    private val initData: MainViewInitData,
    private val dailyTagView: DailyTagView,
    private val eventCardView: EventCardView
) {

    suspend fun initViews() {
        calendarInit() //无论如何都需要初始化calendarView（需要创建视图）
        initData.dailyTagUiState.collect {
            dailyTagView.refreshDailyTag(binding, it) //初次启动刷新DailyTag
        }
        eventCardInit(binding, initData.todayEvent, initData.todayEventPicture)
    }

    private fun eventCardInit(binding: ActivityMainBinding, event: Event,
                              eventPicture: Flow<Drawable?>
    ) {
        val currentDate = systemDate.toDate()
        //活动大于最后一期时，隐藏活动卡片
        if (currentDate - IntDate(event.startDate) >= 13) {
            viewModel.isEventCardVisible = false
            binding.eventCard.eventCardItem.alpha = 0f
        } else {
            viewModel.isEventCardVisible = true
            eventCardView.refreshEventComponent(mainActivity, mainActivity.lifecycleScope,
                event, eventPicture, binding)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun calendarInit() {
        val list = ArrayList<CalendarScrollView>().apply {
            add(getCalendarView(-1, 0))
            add(getCalendarView(0, 1))
            add(getCalendarView(1, 2))
        }
        //初始化viewPager
        val viewPager: ViewPager = mainActivity.findViewById(R.id.viewPager)
        val pagerAdapter = CalendarViewPagerAdapter(list)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 1 //viewPager初始位置为1
        //设置监听器，用来监听滚动（日历翻页）
        viewPager.addOnPageChangeListener(getOnPageChangeListener(viewPager, list, pagerAdapter))
        //观察角色集合的变化，刷新当前月过生日的角色
        viewModel.characterInMonth.observe(mainActivity) {
            if (it.isNotEmpty()) {
                for (calendarView in list) {
                    //由于生日角色只在当前页面刷新，故获取当前显示的view信息
                    if (calendarView.lastPosition == viewModel.calendarCurrentPosition) {
                        val view = calendarView.view as RecyclerView
                        val adapter = view.adapter as CalendarViewAdapter
                        val calendarUtil = adapter.calendarUtil
                        //如果当前view的月份与得到的月份一样，则刷新生日角色
                        if (CharacterUtil.birthdayToMonth(it[0].birthday) == calendarUtil.month) {
                            adapter.birthdayMap.clear()
                            CharacterUtil.characterListToBirthdayMap(it, adapter.birthdayMap)
                            adapter.notifyDataSetChanged()
                            //刷新生日卡片
                            val characterId =
                                adapter.birthdayMap[viewModel.currentDate.value?.day.toString()]
                            if (characterId != null)
                                viewModel.refreshBirthdayCard(characterId)
                            else
                                viewModel.refreshBirthdayCard(0)
                        }
                    }
                }
            }
        }
    }

    private fun getOnPageChangeListener(viewPager: ViewPager, list: List<CalendarScrollView>,
                                        pagerAdapter: CalendarViewPagerAdapter) =
        object : ViewPager.OnPageChangeListener {
        //position为当前位置（正在滚动时就会改变），监听月变化（翻页）
        override fun onPageSelected(position: Int) {
            viewModel.calendarCurrentPosition = position
            for (calendarView in list) {
                //发生月变化（上个位置等于当前位置，说明viewPager已被提前加载，即为左右翻页动作）
                if (calendarView.lastPosition == position) {
                    //获取当前item的日期
                    val view = calendarView.view as RecyclerView
                    val adapter = view.adapter as CalendarViewAdapter
                    val calendarUtil = adapter.calendarUtil
                    val year = calendarUtil.year
                    val month = calendarUtil.month
                    viewModel.getCharacterByMonth(month) //刷新当前月的生日角色
                    val selectedDay = viewModel.selectedItem.value!!
                    val maxDay = calendarUtil.getMaximumDaysInMonth()
                    //刷新当前日期，从而刷新卡片信息
                    viewModel.run {
                        currentDate.value?.year = year
                        currentDate.value?.month = month
                        //判断最大天数是否满足选中天数
                        if (maxDay < selectedDay) {
                            currentDate.value?.day = maxDay
                            setSelectedItem(maxDay)
                        }
                        refreshCurrentDate()
                    }
                }
            }
        }
        override fun onPageScrollStateChanged(state: Int) { //设置循环滚动，滚动到边界自动回正
            when (state) {
                //（滚动）动作完成（无论是滚动还是回弹）
                ViewPager.SCROLL_STATE_IDLE -> {
                    //如果当前位置为0，则设置item为倒数第二
                    if (viewModel.calendarCurrentPosition == 0) {
                        viewPager.setCurrentItem(pagerAdapter.count - 2, false)
                        //如果当前位置为倒数第一，则设置item为1
                    } else if (viewModel.calendarCurrentPosition == pagerAdapter.count - 1) {
                        viewPager.setCurrentItem(1, false)
                    }
                }
            }
            //循环滚动引起的位置变化将在adapter中处理
        }
        override fun onPageScrolled(position: Int, positionOffset: Float,
                                    positionOffsetPixels: Int) {

        }
    }

    //获取ViewPager的单个view(recyclerView)
    @SuppressLint("NotifyDataSetChanged") //当目标日期改变时，刷新RecyclerView
    private fun getCalendarView(relativeMonth: Int, lastPosition: Int): CalendarScrollView {
        val calendar = CalendarUtil()
        calendar.clearDays()
        calendar.setRelativeMonth(relativeMonth)
        val dateList = calendar.getDateList()
        //创建日历recyclerView
        val layoutManager = object : GridLayoutManager(mainActivity, 7) {
            //禁止纵向滚动
            override fun canScrollVertically() = false
        }
        val recyclerView = RecyclerView(mainActivity)
        val adapter = CalendarViewAdapter(mainActivity, dateList, calendar, viewModel)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        //观察选中item的变化，从而设置选中效果
        viewModel.selectedItem.observe(mainActivity) {
            adapter.notifyDataSetChanged()
        }
        return CalendarScrollView(recyclerView, lastPosition)
    }

}