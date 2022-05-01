package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.CalendarScrollView
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.util.CalendarUtil
import com.mty.bangcalendar.logic.util.EventUtil
import com.mty.bangcalendar.logic.util.LogUtil
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        val toolbar: Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        calendarInit() //日历初始化

        //观察当前日期变化，及时刷新活动信息
        viewModel.currentDate.observe(this) {
            val date = it.getDate()
            Log.d("MainActivity", "日期发生变化 $date")
            viewModel.getEventByDate(date) //刷新活动
            mainBinding.date.text = StringBuilder().run {
                append(it.year)
                append("年")
                append(it.month)
                append("月")
            }
        }
        viewModel.refreshCurrentDate() //初次启动刷新当前界面活动组件内容

        //观察活动变化，刷新活动组件内容
        viewModel.event.observe(this) {
            LogUtil.d("Event", "Event id is ${it.id}")
            refreshEventComponent(it, mainBinding)
        }

        //观察活动图片变化，刷新活动图片
        viewModel.eventPicture.observe(this) {
            val responseBody = it.getOrNull()
            if (responseBody != null) {
                val byte = responseBody.bytes()
                val bitmap = BitmapFactory.decodeByteArray(byte, 0, byte.size)
                mainBinding.eventCard.eventBackground.background =
                    BitmapDrawable(this.resources, bitmap)
            } else {
                it.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    private fun refreshEventComponent(event: Event, binding: ActivityMainBinding) {
        //刷新活动状态
        val todayEventId = viewModel.todayEvent.value?.id
        val eventId = event.id
        todayEventId?.let {
            when (true) {
                (eventId < todayEventId) -> binding.eventCard.eventProgressName
                    .setText(R.string.finish)
                (eventId == todayEventId) -> binding.eventCard.eventProgressName
                    .setText(R.string.ing)
                else -> binding.eventCard.eventProgressName.setText(R.string.prepare)
            }
        }
        //刷新活动类型
        binding.eventCard.eventType.text = StringBuilder().run {
            append("活动")
            append(event.id)
            append(" ")
            append(EventUtil.matchType(event.type))
            toString()
        }
        //刷新活动角色
        Glide.with(this).load(EventUtil.matchCharacter(event.character1))
            .into(binding.eventCard.char1)
        Glide.with(this).load(EventUtil.matchCharacter(event.character2))
            .into(binding.eventCard.char2)
        Glide.with(this).load(EventUtil.matchCharacter(event.character3))
            .into(binding.eventCard.char3)
        Glide.with(this).load(EventUtil.matchCharacter(event.character4))
            .into(binding.eventCard.char4)
        Glide.with(this).load(EventUtil.matchCharacter(event.character5))
            .into(binding.eventCard.char5)
        //刷新活动属性
        Glide.with(this).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.eventCard.eventAttrs)
        //刷新乐队图片
        Glide.with(this).load(EventUtil.getBandPic(event)).into(binding.eventCard.eventBand)
        //刷新活动图片
        viewModel.getEventPicture(EventUtil.eventIdFormat(event.id.toInt()))
    }

    private fun calendarInit() {
        val list = ArrayList<CalendarScrollView>().apply {
            add(getCalendarView(-1, 0))
            add(getCalendarView(0, 1))
            add(getCalendarView(1, 2))
        }
        //初始化viewPager
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val pagerAdapter = CalendarViewPagerAdapter(list)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 1 //viewPager初始位置为1
        //设置监听器，用来监听滚动（日历翻页）
        viewPager.addOnPageChangeListener(getOnPageChangeListener(viewPager, list, pagerAdapter))
    }

    private fun getOnPageChangeListener(viewPager: ViewPager, list: List<CalendarScrollView>,
        pagerAdapter: CalendarViewPagerAdapter) = object : ViewPager.OnPageChangeListener {
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
    private fun getCalendarView(relativeMonth: Int, lastPosition: Int): CalendarScrollView {
        val calendar = CalendarUtil()
        calendar.clearDays()
        calendar.setRelativeMonth(relativeMonth)
        val dateList = calendar.getDateList()
        //创建日历recyclerView
        val layoutManager = object : GridLayoutManager(this, 7) {
            //禁止纵向滚动
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        val recyclerView = RecyclerView(this)
        val adapter = CalendarViewAdapter(this, dateList, calendar, viewModel)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        //观察选中item的变化，从而设置选中效果
        viewModel.selectedItem.observe(this) {
            adapter.notifyDataSetChanged()
        }
        return CalendarScrollView(recyclerView, lastPosition)
    }

}