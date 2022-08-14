package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.enum.EventConstant
import com.mty.bangcalendar.logic.model.CalendarScrollView
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.network.ServiceCreator
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.list.CharacterListActivity
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.search.SearchActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*
import java.util.regex.Pattern

class MainActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //小白条沉浸
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            mainBinding.mainActivity.setOnApplyWindowInsetsListener { view, insets ->
                val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.updatePadding(top = top)
                (mainBinding.floatButton.layoutParams as FrameLayout.LayoutParams)
                    .bottomMargin = top
                (mainBinding.goBackFloatButton.layoutParams as FrameLayout.LayoutParams)
                    .bottomMargin = top
                insets
            }
        }

        //活动进度条初始化
        mainBinding.eventCard.eventProgress.run {
            progressColor = getColor(ThemeUtil.getThemeColor(this@MainActivity))
            textColor = getColor(ThemeUtil.getThemeColor(this@MainActivity))
        }

        calendarInit() //日历初始化

        //观察当前日期变化，及时刷新活动信息
        viewModel.currentDate.observe(this) {
            val date = it.getDate()
            LogUtil.d("MainActivity", "日期发生变化 $date")
            viewModel.getEventByDate(date) //刷新活动
            //顶部日期刷新
            mainBinding.date.text = StringBuilder().run {
                append(it.year)
                append("年")
                append(it.month)
                append("月")
                toString()
            }
            //返回今天浮窗
            if (it.year == systemDate.year && it.month == systemDate.month
                && it.day== systemDate.day) {
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

        viewModel.getCharacterByMonth(systemDate.month) //首次启动刷新当前月的生日角色

        viewModel.birthdayCard.observe(this) {
            when (it) {
                0 -> mainBinding.birCard.visibility = View.GONE
                else -> refreshBirthdayCard(it, mainBinding)
            }
        }

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
                val birthdayAway = CharacterUtil.birthdayAway(it.birthday, systemDate)
                viewModel.birthdayAway = birthdayAway
            }
            refreshDailyTag(mainBinding)
        }

        viewModel.preferenceNearlyBandEvent.observe(this) { event ->
            event?.let {
                LogUtil.d("Event", "乐队偏好近期活动为${event.id}")
                refreshDailyTag(mainBinding)
            }
        }

        viewModel.preferenceBand.observe(this) { bandName ->
            bandName?.let {
                if (it == EventConstant.OTHER.describe
                    || it == EventUtil.getBand(viewModel.todayEvent.value!!).describe) {
                    refreshDailyTag(mainBinding)
                } else {
                    viewModel.getPreferenceNearlyBandEvent(EventUtil.bandNameToCharacter1(it))
                }
            }
        }

        //附加提示
        viewModel.additionalTip.observe(this) {
            refreshDailyTag(mainBinding)
        }
        viewModel.getAdditionalTip()

        viewModel.todayEvent.observe(this) { event ->
            viewModel.run {
                LogUtil.d("Event", "本期活动序号为：${event?.id}")
                eventStartTime = EventUtil.getEventStartTime(event)
                eventEndTime = EventUtil.getEventEndTime(event)
                event?.let {
                    //如果activity意外重启，若当前活动不为当日活动，则不刷新活动状态（说明不是初次启动）
                    if (EventUtil.isSameEvent(mainBinding, event.id.toInt()))
                        refreshEventStatus(event, mainBinding) //初次启动刷新活动状态
                }
                getPreferenceBand() //初次启动刷新关注的乐队
            }
        }
        viewModel.getTodayEvent() //获取当天活动

        //返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            goBackToSystemDate(mainBinding)
        }

        //额外的提醒按钮
        mainBinding.floatButton.setOnClickListener {
            addAdditionalTip()
        }

        viewModel.jumpDate.observe(this) {
            jumpDate(mainBinding.viewPager, CalendarUtil.dateToCalendarUtil(it))
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
            R.id.menu_jump -> {
                chooseDate(findViewById(R.id.viewPager))
            }
            R.id.app_bar_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun addAdditionalTip() {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.additional_tip, null, false)
        val editText: EditText = view.findViewById(R.id.additionalText)
        val oldTip = viewModel.additionalTip.value
        editText.setText(oldTip)
        val dialog = AlertDialog.Builder(this)
            .setTitle("额外的日程提醒")
            .setIcon(R.mipmap.ic_launcher)
            .setView(view)
            .setNegativeButton("取消") { _, _ ->
            }
            .setPositiveButton("提交") { _, _ ->
                val newTip = editText.text.toString()
                if (oldTip != newTip)
                    viewModel.setAdditionalTip(newTip)
            }
            .create()
        dialog.show()
    }

    //刷新dailyTag
    private fun refreshDailyTag(mainBinding: ActivityMainBinding) {
        val stringBuilder = StringBuilder()
        val userName = viewModel.userName.value
        val characterName = viewModel.preferenceCharacter.value?.name
        val birthdayAway = viewModel.birthdayAway
        val bandName = viewModel.preferenceBand.value
        val todayEvent = viewModel.todayEvent.value
        val nearlyBandEvent = viewModel.preferenceNearlyBandEvent.value
        val additionalTip = viewModel.additionalTip.value

        if(userName == "") {
            stringBuilder.append("${systemDate.getTimeName()}好，邦邦人。")
        } else {
            stringBuilder.append("${systemDate.getTimeName()}好，$userName。")
        }
        stringBuilder.append(getString(R.string.defaultTag))
        characterName?.let {
            if (birthdayAway == 0) stringBuilder.append("今天是${it}的生日，生日快乐！")
            else if (birthdayAway != null)
                stringBuilder.append("距离${it}的生日还有${birthdayAway}天。")
            else stringBuilder.append("")
        }
        if (bandName != null && bandName != EventConstant.OTHER.describe && todayEvent != null) {
            if (bandName == EventUtil.getBand(todayEvent).describe) {
                stringBuilder.append("这期活动是${bandName}活哦，快去冲榜吧。")
            } else if (nearlyBandEvent != null) {
                stringBuilder.append("距离下次${bandName}活还有" +
                        "${CalendarUtil.differentOfTwoDates(systemDate.getDate(), 
                            nearlyBandEvent.startDate)}天，活动编号为${nearlyBandEvent.id}，" +
                        "活动属性为${EventUtil.getAttrsName(nearlyBandEvent.attrs)}。")
            }
        }
        if (additionalTip != null && additionalTip != "") {
            val strs = additionalTip.split(" ")
            if (strs.size == 2) {
                val regex = "\\b\\d{8}\\b"
                val pattern = Pattern.compile(regex)
                val matcher = pattern.matcher(strs[1])
                //输入正确再进行对比
                if (matcher.find()) {
                    val systemDate = systemDate.getDate()
                    val targetDate = Integer.parseInt(strs[1])
                    val differentOfTwoDates = CalendarUtil
                        .differentOfTwoDates(systemDate, targetDate).toInt()
                    when (true) {
                        (differentOfTwoDates > 0) -> {
                            stringBuilder.append("距离${strs[0]}还有${differentOfTwoDates}天。")
                        }
                        (differentOfTwoDates == 0) -> {
                            stringBuilder.append("今天就是${strs[0]}。")
                        }
                        (differentOfTwoDates < 0) -> {
                            stringBuilder.append("距离${strs[0]}已经过去" +
                                    "${differentOfTwoDates * -1}天。")
                        }
                        else -> {}
                    }
                }
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
            binding.birChar1.setOnClickListener {
                startCharacterListActivity(12)
            }
            binding.birChar2.setOnClickListener {
                startCharacterListActivity(17)
            }
            binding.birChar2.visibility = View.VISIBLE
        }else {
            binding.birChar2.visibility = View.GONE
            Glide.with(this).load(CharacterUtil.matchCharacter(id))
                .into(binding.birChar1)
            binding.birChar1.setOnClickListener {
                startCharacterListActivity(id)
            }
            binding.birChar2.setOnClickListener {
                startCharacterListActivity(id)
            }
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
        binding.eventCard.char1.setOnClickListener {
            startCharacterListActivity(event.character1)
        }
        Glide.with(this).load(EventUtil.matchCharacter(event.character2))
            .into(binding.eventCard.char2)
        binding.eventCard.char2.setOnClickListener {
            startCharacterListActivity(event.character2)
        }
        Glide.with(this).load(EventUtil.matchCharacter(event.character3))
            .into(binding.eventCard.char3)
        binding.eventCard.char3.setOnClickListener {
            startCharacterListActivity(event.character3)
        }
        Glide.with(this).load(EventUtil.matchCharacter(event.character4))
            .into(binding.eventCard.char4)
        if (event.character4 != -1) {
            binding.eventCard.char4.setOnClickListener {
                startCharacterListActivity(event.character4)
            }
        }
        Glide.with(this).load(EventUtil.matchCharacter(event.character5))
            .into(binding.eventCard.char5)
        if (event.character5 != -1) {
            binding.eventCard.char5.setOnClickListener {
                startCharacterListActivity(event.character5)
            }
        }
        //刷新活动属性
        Glide.with(this).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.eventCard.eventAttrs)
        //刷新乐队图片
        Glide.with(this).load(EventUtil.getBandPic(event))
            .into(binding.eventCard.eventBand)
        binding.eventCard.eventBand.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            intent.putExtra("current_id", event.id.toInt())
            intent.putExtra("band_id", EventUtil.getBand(event).id)
            startActivity(intent)
        }
        //刷新活动图片
        val eventId = EventUtil.eventIdFormat(event.id.toInt())
        val uri = Uri.parse(ServiceCreator.BASE_URL +
                "event/banner_memorial_event$eventId.png")
        Glide.with(this).load(uri).apply(viewModel.glideOptions)
            .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                binding.eventCard.eventBackground.background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                //
            }
        })
        binding.eventCard.eventButton.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            intent.putExtra("current_id", event.id.toInt())
            startActivity(intent)
        }
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
                    val systemTime = systemDate.getTimeInMillis()
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

    private fun chooseDate(viewPager: ViewPager) {
        val view = layoutInflater.inflate(R.layout.date_picker, null)
        val datePicker: DatePicker = view.findViewById(R.id.datePicker)
        val floatButton: FloatingActionButton = findViewById(R.id.floatButton)
        val dialog = AlertDialog.Builder(this)
            .setTitle("请选择跳转的日期")
            .setIcon(R.mipmap.ic_launcher)
            .setView(view)
            .setNegativeButton("取消") { _, _ ->
            }
            .setPositiveButton("确认") { _, _ ->
                if (floatButton.visibility == View.VISIBLE) { //原地选择不跳转
                    val calendarUtil = CalendarUtil().apply {
                        year = datePicker.year
                        month = datePicker.month + 1
                        day = datePicker.dayOfMonth
                    }
                    jumpDate(viewPager, calendarUtil)
                }
            }
            .create()
        dialog.show()
    }

    private fun goBackToSystemDate(mainBinding: ActivityMainBinding) {
        jumpDate(mainBinding.viewPager, systemDate)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun jumpDate(viewPager: ViewPager, target: CalendarUtil) {
        val viewPagerAdapter = viewPager.adapter as CalendarViewPagerAdapter
        val scrollViewList = viewPagerAdapter.views
        var lastPosition = 0
        var relativeMonth = -1
        //初始化view集合中view的日期
        for (scrollView in scrollViewList) {
            scrollView.lastPosition = lastPosition++ //设置上次位置
            val viewAdapter = (scrollView.view as RecyclerView)
                .adapter as CalendarViewAdapter
            val calendarUtil = viewAdapter.calendarUtil
            calendarUtil.year = target.year
            calendarUtil.month = target.month
            calendarUtil.rows = target.rows
            calendarUtil.setRelativeMonth(relativeMonth++)
            viewAdapter.dateList.run {
                this as ArrayList
                clear()
                addAll(calendarUtil.getDateList())
            }
            viewAdapter.notifyDataSetChanged()
        }
        //初始化viewPager的当前item
        viewPager.currentItem = 1
        //初始化选中项
        viewModel.setSelectedItem(target.day)
        viewModel.currentDate.value?.let {
            it.year = target.year
            it.month = target.month
            it.day = target.day
            it.rows = target.rows
        }
        viewModel.refreshCurrentDate() //刷新卡片信息
        viewModel.getCharacterByMonth(target.month) //刷新角色生日
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

    private fun startCharacterListActivity(id: Int) {
        val intent =
            Intent(this, CharacterListActivity::class.java)
        intent.putExtra("current_id", id)
        startActivity(intent)
    }

}