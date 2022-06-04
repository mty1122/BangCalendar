package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.CalendarScrollView
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mainBinding.eventCard.eventProgress.run {
            progressColor = getColor(R.color.progress_color)
            textColor = getColor(R.color.progress_color)
        }

        calendarInit() //日历初始化

        //观察当前日期变化，及时刷新活动信息
        viewModel.currentDate.observe(this) {
            val date = it.getDate()
            LogUtil.d("MainActivity", "日期发生变化 $date")
            viewModel.getEventByDate(date) //刷新活动
            mainBinding.date.text = StringBuilder().run {
                append(it.year)
                append("年")
                append(it.month)
                append("月")
                toString()
            }
            //返回今天浮窗
            if (it.year == viewModel.systemDate.year && it.month == viewModel.systemDate.month
                && it.day== viewModel.systemDate.day) {
                mainBinding.goBackFloatButton.visibility = View.GONE
            } else {
                mainBinding.goBackFloatButton.visibility = View.VISIBLE
            }
        }

        //观察活动变化，刷新活动组件内容
        viewModel.event.observe(this) {
            val currentDate = viewModel.currentDate.value!!.getDate()
            //活动小于第一期或者大于最后一期时，隐藏活动卡片
            if (it == null || CalendarUtil.differentOfTwoDates(it.startDate, currentDate) >= 7) {
                LogUtil.d("Event", "currentDate $currentDate startDate ${it?.startDate}")
                mainBinding.eventCard.eventCardItem.visibility = View.GONE
            } else {
                mainBinding.eventCard.eventCardItem.visibility = View.VISIBLE
                LogUtil.d("Event", "Event id is ${it.id}")
                //相同活动之间移动，不刷新活动
                if (!EventUtil.isSameEvent(mainBinding, it.id.toInt())) {
                    refreshEventComponent(it, mainBinding)
                }
            }
        }

        //观察活动图片变化，刷新活动图片
        viewModel.eventPicture.observe(this) {
            val responseBody = it.getOrNull()
            if (responseBody != null) {
                try {
                    val byte = responseBody.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(byte, 0, byte.size)
                    mainBinding.eventCard.eventBackground.background =
                        BitmapDrawable(this.resources, bitmap)
                } catch (e: Exception) {
                    LogUtil.i("Internet", "获取不到返回Body，可能是屏幕发生旋转")
                }
            } else {
                it.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.getCharacterByMonth(viewModel.systemDate.month) //首次启动刷新当前月的生日角色

        viewModel.birthdayCard.observe(this) {
            when (it) {
                0 -> mainBinding.birCard.visibility = View.GONE
                else -> refreshBirthdayCard(it, mainBinding)
            }
        }

        viewModel.todayEvent.observe(this) { event ->
            viewModel.run {
                LogUtil.d("MainActivity", "本期活动序号为：${event?.id}")
                eventStartTime = EventUtil.getEventStartTime(event)
                eventEndTime = EventUtil.getEventEndTime(event)
                event?.let {
                    refreshEventStatus(event, mainBinding) //初次启动刷新活动状态
                }
                getPreferenceBand() //初次启动刷新关注的乐队
            }
        }
        viewModel.getTodayEvent() //获取当天活动

        //dailyTag服务
        viewModel.userName.observe(this) {
            refreshDailyTag(mainBinding)
        }
        viewModel.getUserName()

        viewModel.preferenceCharacterId.observe(this) {
            viewModel.getPreferenceCharacter(it)
        }
        viewModel.getPreferenceCharacterId()

        viewModel.preferenceCharacter.observe(this) { character ->
            viewModel.birthdayAway = null
            character?.let {
                val birthdayAway = CharacterUtil.birthdayAway(it.birthday, viewModel.systemDate)
                viewModel.birthdayAway = birthdayAway
            }
            refreshDailyTag(mainBinding)
        }

        viewModel.preferenceBand.observe(this) {
            refreshDailyTag(mainBinding)
        }

        //返回今天
        mainBinding.goBackFloatButton.setOnClickListener {
            goBackToSystemDate(mainBinding)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    //刷新dailyTag
    private fun refreshDailyTag(mainBinding: ActivityMainBinding) {
        val stringBuilder = StringBuilder()
        val userName = viewModel.userName.value
        val characterName = viewModel.preferenceCharacter.value?.name
        val birthdayAway = viewModel.birthdayAway
        val bandName = viewModel.preferenceBand.value
        val todayEvent = viewModel.todayEvent.value

        if(userName == "") {
            stringBuilder.append("${viewModel.systemDate.getTimeName()}好，邦邦人。")
        } else {
            stringBuilder.append("${viewModel.systemDate.getTimeName()}好，$userName。")
        }
        stringBuilder.append(getString(R.string.defaultTag))
        characterName?.let {
            if (birthdayAway == 0) stringBuilder.append("今天是${it}的生日，生日快乐！")
            else if (birthdayAway != null)
                stringBuilder.append("距离${it}的生日还有${birthdayAway}天。")
            else stringBuilder.append("")
        }
        if (bandName != null && todayEvent != null) {
            if (bandName == EventUtil.matchBand(todayEvent)) {
                stringBuilder.append("这期活动是${bandName}活哦，快去冲榜吧。")
            }
        }
        mainBinding.dailytag.text = stringBuilder.toString()
    }

    //刷新生日卡片
    private fun refreshBirthdayCard(id: Int, binding: ActivityMainBinding) {
        LogUtil.d("MainActivity", "生日卡片刷新")
        if (id == 12 || id == 17) {
            Glide.with(this).load(CharacterUtil.matchCharacter(12))
                .into(binding.birChar1)
            Glide.with(this).load(CharacterUtil.matchCharacter(17))
                .into(binding.birChar2)
            binding.birChar2.visibility = View.VISIBLE
        }else {
            binding.birChar2.visibility = View.GONE
            Glide.with(this).load(CharacterUtil.matchCharacter(id)).into(binding.birChar1)
        }
        binding.birCard.visibility = View.VISIBLE
    }

    private fun refreshEventComponent(event: Event, binding: ActivityMainBinding) {
        LogUtil.d("MainActivity", "刷新活动组件")
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

    private fun refreshEventStatus(event: Event, binding: ActivityMainBinding) {
        val todayEventId = viewModel.todayEvent.value?.id
        val eventId = event.id
        todayEventId?.let {
            LogUtil.d("MainActivity", "刷新活动状态")
            when (true) {
                (eventId < todayEventId) -> {
                    binding.eventCard.eventProgressName.setText(R.string.finish)
                    binding.eventCard.eventProgress.progress = 100
                }
                (eventId == todayEventId) -> {
                    val systemTime = viewModel.systemDate.getTimeInMillis()
                    val eventStartTime = viewModel.eventStartTime
                    val eventEndTime = viewModel.eventEndTime
                    if (eventStartTime != null && eventEndTime != null) {
                        when (true) {
                            (systemTime < eventStartTime) -> {
                                binding.eventCard.eventProgressName.setText(R.string.prepare)
                                binding.eventCard.eventProgress.progress = 0
                            }
                            (systemTime >= eventEndTime) -> {
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

    @SuppressLint("NotifyDataSetChanged")
    private fun goBackToSystemDate(mainBinding: ActivityMainBinding) {
        val viewPagerAdapter = mainBinding.viewPager.adapter as CalendarViewPagerAdapter
        val scrollViewList = viewPagerAdapter.views
        var lastPosition = 0
        var relativeMonth = -1
        //初始化view集合中view的日期
        for (scrollView in scrollViewList) {
            scrollView.lastPosition = lastPosition++ //设置上次位置
            val viewAdapter = (scrollView.view as RecyclerView)
                .adapter as CalendarViewAdapter
            val calendarUtil = viewAdapter.calendarUtil
            calendarUtil.year = viewModel.systemDate.year
            calendarUtil.month = viewModel.systemDate.month
            calendarUtil.rows = viewModel.systemDate.rows
            calendarUtil.setRelativeMonth(relativeMonth++)
            viewAdapter.dateList.run {
                this as ArrayList
                clear()
                addAll(calendarUtil.getDateList())
            }
            viewAdapter.notifyDataSetChanged()
        }
        //初始化viewPager的当前item
        mainBinding.viewPager.currentItem = 1
        //初始化选中项
        viewModel.setSelectedItem(viewModel.systemDate.day)
        viewModel.currentDate.value?.let {
            it.year = viewModel.systemDate.year
            it.month = viewModel.systemDate.month
            it.day = viewModel.systemDate.day
            it.rows = viewModel.systemDate.rows
        }
        viewModel.refreshCurrentDate() //刷新卡片信息
        viewModel.getCharacterByMonth(viewModel.systemDate.month) //刷新角色生日
    }

    @SuppressLint("NotifyDataSetChanged")
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
        //观察角色集合的变化，刷新当前月过生日的角色
        viewModel.characterInMonth.observe(this) {
            if (it.isNotEmpty()) {
                val birthdayMap = CharacterUtil.characterListToBirthdayMap(it)
                for (calendarView in list) {
                    //由于生日角色只在当前页面刷新，故获取当前显示的view信息
                    if (calendarView.lastPosition == viewModel.calendarCurrentPosition) {
                        val view = calendarView.view as RecyclerView
                        val adapter = view.adapter as CalendarViewAdapter
                        val calendarUtil = adapter.calendarUtil
                        //如果当前view的月份与得到的月份一样，则刷新生日角色
                        if (CharacterUtil.birthdayToMonth(it[0].birthday) == calendarUtil.month) {
                            adapter.birthdayMap.clear()
                            adapter.birthdayMap.putAll(birthdayMap)
                            adapter.notifyDataSetChanged()
                            //刷新生日卡片
                            viewModel.refreshBirthdayCard(0)
                            adapter.birthdayMap[viewModel.currentDate.value?.day.toString()]
                                ?.let { id ->
                                viewModel.refreshBirthdayCard(id)
                            }
                        }
                    }
                }
            }
        }

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
        calendar.refreshRows()
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