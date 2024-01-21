package com.mty.bangcalendar.ui.main.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.main.adapter.CalendarViewAdapter
import com.mty.bangcalendar.ui.main.adapter.CalendarViewPagerAdapter
import com.mty.bangcalendar.ui.main.state.CalendarItemUiState
import com.mty.bangcalendar.util.CalendarUtil
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class CalendarView @Inject constructor(@ActivityContext val context: Context) {

    //记录参与滚动的view在viewPager中的上个位置
    class CalendarScrollView(val view: View, var lastPosition: Int)

    private var calendarCurrentPosition = 1 //当前（显示在屏幕上的）view在viewPager中的位置

    suspend fun calendarInit(
        viewPager: ViewPager,
        initDate: IntDate?,
        lifecycleScope: LifecycleCoroutineScope,
        onDateChange: (IntDate) -> Unit,
        getCurrentDate: () -> IntDate,
        fetchBirthdayMapByMonth: suspend (Int) -> Map<String, Int>
    ) {
        val list = List(3) { position->
            //上次位置初始为当前位置，左右两个看不见的日历视图与中间的相差一个月
            createCalendarScrollView(relativeMonth = position - 1, lastPosition =  position,
                initDate, onDateChange, getCurrentDate, fetchBirthdayMapByMonth)
        }
        //初始化viewPager
        val pagerAdapter = CalendarViewPagerAdapter(list, lifecycleScope) { month->
            fetchBirthdayMapByMonth(month)
        }
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 1 //viewPager初始位置为1
        //设置监听器，用来监听滚动（日历翻页）
        viewPager.addOnPageChangeListener(getOnPageChangeListener(viewPager, list, pagerAdapter))
    }

    //实现跳转日期
    @SuppressLint("NotifyDataSetChanged")
    suspend fun jumpDate(
        viewPager: ViewPager,
        target: CalendarUtil,
        onDateChange: (IntDate) -> Unit,
        fetchBirthdayMapByMonth: suspend (Int) -> Map<String, Int>
    ) {
        //刷新当前日期
        onDateChange(target.toDate())
        val viewPagerAdapter = viewPager.adapter as CalendarViewPagerAdapter
        val scrollViewList = viewPagerAdapter.views
        //初始化view集合中view的日期
        for ((position, scrollView) in scrollViewList.withIndex()) {
            scrollView.lastPosition = position //上次位置初始为当前位置
            val viewAdapter = (scrollView.view as RecyclerView)
                .adapter as CalendarViewAdapter
            val calendarUtil = viewAdapter.calendarUtil
            calendarUtil.year = target.year
            calendarUtil.month = target.month + position - 1 //左右两个看不见的日历视图与中间的相差一个月
            //获取初始数据
            val dateList = calendarUtil.getDateList()
            val birthdayMap = fetchBirthdayMapByMonth(calendarUtil.month)
            val calendarItemUiState = viewAdapter.uiState.copy(
                isVisible = position == 1,
                dateList = dateList,
                birthdayMap = birthdayMap
            )
            viewAdapter.uiState = calendarItemUiState
            //刷新日历
            viewAdapter.notifyDataSetChanged()
        }
        //初始化viewPager的当前item
        viewPager.currentItem = 1
    }

    //创建ViewPager的单个view(recyclerView)
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun createCalendarScrollView(
        relativeMonth: Int,
        lastPosition: Int,
        initDate: IntDate?,
        onDateChange: (IntDate) -> Unit,
        getCurrentDate: () -> IntDate,
        fetchBirthdayMapByMonth: suspend (Int) -> Map<String, Int>
    ): CalendarScrollView{
        val calendar = CalendarUtil(initDate)
        calendar.clearDays()
        calendar.month += relativeMonth
        //创建日历recyclerView
        val layoutManager = object : GridLayoutManager(context, 7) {
            //禁止纵向滚动
            override fun canScrollVertically() = false
        }
        val recyclerView = RecyclerView(context)
        //禁用item动画
        recyclerView.itemAnimator = null
        //获取初始数据
        val dateList = calendar.getDateList()
        val birthdayMap = fetchBirthdayMapByMonth(calendar.month)
        val calendarItemUiState = CalendarItemUiState(
            isVisible = lastPosition == 1, //设置中间的日历视图可见
            dateList = dateList,
            birthdayMap = birthdayMap,
            getCurrentDate = getCurrentDate,
            onDateChange = onDateChange
        )
        //设置适配器
        val adapter = CalendarViewAdapter(context, calendarItemUiState, calendar)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        return CalendarScrollView(recyclerView, lastPosition)
    }

    private fun getOnPageChangeListener(viewPager: ViewPager, list: List<CalendarScrollView>,
                                        pagerAdapter: CalendarViewPagerAdapter) =
        object : ViewPager.OnPageChangeListener {
            //position为当前位置（正在滚动时就会改变），监听月变化（翻页）
            override fun onPageSelected(position: Int) {
                calendarCurrentPosition = position
                for (calendarView in list) {
                    val view = calendarView.view as RecyclerView
                    val adapter = view.adapter as CalendarViewAdapter
                    //发生月变化（上个位置等于当前位置，说明viewPager已被提前加载，即为左右翻页动作）
                    if (calendarView.lastPosition == position) {
                        //获取当前item的日期
                        val calendarUtil = adapter.calendarUtil
                        val year = calendarUtil.year
                        val month = calendarUtil.month
                        var selectedDay = adapter.uiState.getCurrentDate().getDay()
                        val maxDay = calendarUtil.getMaximumDaysInMonth()
                        //刷新当前日期，从而刷新卡片信息
                        if (maxDay < selectedDay) {
                            selectedDay = maxDay
                        }
                        adapter.uiState.onDateChange(
                            CalendarUtil.getDate(year, month, selectedDay)
                        )
                        //设置可见性，显示选中项
                        adapter.uiState = adapter.uiState.copy(
                            isVisible = true
                        )
                        adapter.showSelectedItem()
                        //取消处于不可见位置的view的选中
                    } else {
                        adapter.uiState = adapter.uiState.copy(
                            isVisible = false
                        )
                        adapter.hideSelectedItem()
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) { //设置循环滚动，滚动到边界自动回正
                when (state) {
                    //（滚动）动作完成（无论是滚动还是回弹）
                    ViewPager.SCROLL_STATE_IDLE -> {
                        //如果当前位置为0，则设置item为倒数第二
                        if (calendarCurrentPosition == 0) {
                            viewPager.setCurrentItem(pagerAdapter.count - 2, false)
                            //如果当前位置为倒数第一，则设置item为1
                        } else if (calendarCurrentPosition == pagerAdapter.count - 1) {
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

}