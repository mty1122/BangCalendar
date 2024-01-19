package com.mty.bangcalendar.ui.main.view

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.ViewTreeObserver
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
import com.mty.bangcalendar.ui.main.state.CalendarItemUiState
import com.mty.bangcalendar.ui.main.state.MainUiState
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.CharacterUtil
import com.mty.bangcalendar.util.GenericUtil
import com.mty.bangcalendar.util.ThemeUtil
import kotlinx.coroutines.flow.Flow

class MainViewInitializer(
    private val mainActivity: MainActivity,
    private val binding: ActivityMainBinding,
    private val viewModel: MainViewModel,
    private val initData: MainViewInitData,
    private val mainUiState: MainUiState,
    private val dailyTagView: DailyTagView,
    private val eventCardView: EventCardView,
    private val birthdayCardView: BirthdayCardView
) {

    suspend fun initViews() {
        //活动进度条初始化
        binding.eventCard.eventProgress.run {
            progressColor = mainActivity.getColor(ThemeUtil.getThemeColor(mainActivity))
            textColor = mainActivity.getColor(ThemeUtil.getThemeColor(mainActivity))
        }

        /* 下方为四大组件初始化（日历、dailyTag、生日卡片、活动卡片） */
        calendarInit() //无论如何都需要初始化calendarView（需要创建视图）
        initData.dailyTagUiState.collect {
            dailyTagView.refreshDailyTag(mainActivity, viewModel, binding, it) //初次启动刷新DailyTag
        }
        eventCardInit(binding, initData.currentEvent, initData.eventPicture)
        initData.birthdayCardUiState.collect{
            //等待其膨胀完成后再初始化，防止获取不到高度
            binding.birCard.cardView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    birCardInit(it, binding)
                    binding.birCard.cardView.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    //生日卡片初始化
    private fun birCardInit(id: Int, binding: ActivityMainBinding) {
        if (id > 0) {
            birthdayCardView.refreshBirthdayCard(mainActivity, id, binding)
            birthdayCardView.isBirthdayCardVisible = true
        }
        else {
            val mainLinearLayout = binding.mainView
            val birCardIndex = mainLinearLayout.indexOfChild(binding.birCardParent)

            //使用初始高度
            val cardHeight = binding.birCard.cardView.height.toFloat()
            val translationY = -cardHeight - GenericUtil.dpToPx(10)

            for (i in birCardIndex + 1 until mainLinearLayout.childCount) {
                val cardBelow = mainLinearLayout.getChildAt(i)
                cardBelow.translationY = translationY
            }
            binding.birCard.cardView.translationY = translationY
            birthdayCardView.isBirthdayCardVisible = false
        }
    }

    private fun eventCardInit(binding: ActivityMainBinding, event: Event?,
                              eventPicture: Flow<Drawable?>?
    ) {
        val currentDate = systemDate.toDate()
        if (event == null || currentDate - IntDate(event.startDate) >= 13) {
            eventCardView.isEventCardVisible = false
            binding.eventCard.eventCardItem.alpha = 0f
        } else {
            eventCardView.isEventCardVisible = true
            eventCardView.refreshEventComponent(mainActivity, mainActivity.lifecycleScope,
                mainUiState, event, eventPicture!!, binding)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun calendarInit() {
        val list = listOf(
            getCalendarView(-1, 0),
            getCalendarView(0, 1),
            getCalendarView(1, 2)
        )
        //初始化viewPager
        val viewPager: ViewPager = mainActivity.findViewById(R.id.viewPager)
        val pagerAdapter = CalendarViewPagerAdapter(list, mainActivity.lifecycleScope) { month->
            val characterList = viewModel.fetchCharacterByMonth(month)
            CharacterUtil.characterListToBirthdayMap(characterList)
        }
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 1 //viewPager初始位置为1
        //设置监听器，用来监听滚动（日历翻页）
        viewPager.addOnPageChangeListener(getOnPageChangeListener(viewPager, list, pagerAdapter))
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
    private suspend fun getCalendarView(relativeMonth: Int, lastPosition: Int): CalendarScrollView{
        val calendar = CalendarUtil()
        calendar.clearDays()
        calendar.month += relativeMonth
        //创建日历recyclerView
        val layoutManager = object : GridLayoutManager(mainActivity, 7) {
            //禁止纵向滚动
            override fun canScrollVertically() = false
        }
        val recyclerView = RecyclerView(mainActivity)
        //获取初始数据
        val dateList = calendar.getDateList()
        val characterList = viewModel.fetchCharacterByMonth(calendar.month)
        val birthdayMap = CharacterUtil.characterListToBirthdayMap(characterList)
        val calendarItemUiState = CalendarItemUiState(
            dateList = dateList,
            birthdayMap = birthdayMap
        )
        //设置适配器
        val adapter = CalendarViewAdapter(mainActivity, calendarItemUiState, calendar, viewModel)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        return CalendarScrollView(recyclerView, lastPosition)
    }

}