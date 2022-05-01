package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
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
import com.mty.bangcalendar.logic.util.CalendarUtil
import com.mty.bangcalendar.logic.util.EventUtil
import com.mty.bangcalendar.logic.util.LogUtil

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

        viewModel.currentDate.observe(this) {
            Log.d("MainActivity", "日期发生变化 ${it.getCalendar().time}")
            refreshEvent(it, mainBinding)
        }
        viewModel.refreshCurrentDate()

        val progressBar: ProgressBar = findViewById(R.id.eventProgress)
        progressBar.visibility = View.INVISIBLE

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    private fun refreshEvent(calendarUtil: CalendarUtil, binding: ActivityMainBinding) {
        val year = calendarUtil.year * 10000
        val month = calendarUtil.month * 100
        val day = calendarUtil.day
        val date = year + month + day
        LogUtil.d("Calendar", "date is $date")
        viewModel.getEventByDate(date)
        viewModel.event.observe(this) {
            LogUtil.d("Event", "Event ID ${it.id}\n${EventUtil.matchType(it.type)}")
            LogUtil.d("Event", "${binding.eventCard.eventType.text}")
            binding.eventCard.eventType.text = EventUtil.matchType(it.type)
            Glide.with(this).load(EventUtil.matchCharacter(it.character1)).into(binding.eventCard.char1)
            Glide.with(this).load(EventUtil.matchCharacter(it.character2)).into(binding.eventCard.char2)
            Glide.with(this).load(EventUtil.matchCharacter(it.character3)).into(binding.eventCard.char3)
            Glide.with(this).load(EventUtil.matchCharacter(it.character4)).into(binding.eventCard.char4)
            Glide.with(this).load(EventUtil.matchCharacter(it.character5)).into(binding.eventCard.char5)
        }
    }

    private fun calendarInit() {
        val list = ArrayList<CalendarScrollView>().apply {
            add(getCalendarView(-1, 0))
            add(getCalendarView(0, 1))
            add(getCalendarView(1, 2))
        }
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val pagerAdapter = CalendarViewPagerAdapter(list)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 1 //viewPager初始位置为1
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            //position为当前位置（正在滚动时就会改变），监听月变化（翻页）
            override fun onPageSelected(position: Int) {
                viewModel.calendarCurrentPosition = position
                for (calendarView in list) {
                    //发生月变化
                    if (calendarView.lastPosition == position) {
                        val view = calendarView.view as RecyclerView
                        val adapter = view.adapter as CalendarViewAdapter
                        val calendarUtil = adapter.calendarUtil
                        val year = calendarUtil.year
                        val month = calendarUtil.month
                        val selectedDay = viewModel.selectedItem.value!!
                        val maxDay = calendarUtil.getMaximumDaysInMonth()
                        //判断最大天数是否满足选中天数
                        if (maxDay < selectedDay) {
                            viewModel.setSelectedItem(maxDay)
                        }
                        LogUtil.d("CalendarViewItem", "position：$position")
                        LogUtil.d("CalendarViewItem", "date：${calendarUtil.getCalendar().time}")
                        //刷新当前日期
                        val currentCalendar = viewModel.currentDate.value
                        currentCalendar?.year = year
                        currentCalendar?.month = month
                        viewModel.refreshCurrentDate()
                    }
                }
            }
            //循环滚动
            override fun onPageScrollStateChanged(state: Int) {
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
            }

            override fun onPageScrolled(position: Int, positionOffset: Float,
                positionOffsetPixels: Int) {

            }
        })
    }

    //获取ViewPager的单个view
    @SuppressLint("NotifyDataSetChanged") //当目标日期改变时，刷新RecyclerView
    private fun getCalendarView(relativeMonth: Int, lastPosition: Int): CalendarScrollView {
        val calendar = CalendarUtil()
        calendar.clearDays()
        calendar.setRelativeMonth(relativeMonth)
        val dateList = calendar.getDateList()
        //创建日历recyclerView
        val layoutManager = object : GridLayoutManager(this, 7) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        val recyclerView = RecyclerView(this)
        val adapter = CalendarViewAdapter(this, dateList, calendar, viewModel)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        viewModel.selectedItem.observe(this) {
            adapter.notifyDataSetChanged()
        }

        return CalendarScrollView(recyclerView, lastPosition)
    }

}