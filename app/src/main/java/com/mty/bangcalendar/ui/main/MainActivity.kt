package com.mty.bangcalendar.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.CalendarScrollView
import com.mty.bangcalendar.logic.model.DailyTag
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.list.CharacterListActivity
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.search.SearchActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    companion object {
        const val COMPONENT_AMOUNTS = 3 //主界面组件的数量
    }

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        //初次启动时，状态栏颜色与引导界面一致
        if (viewModel.isActivityFirstStart)
            window.statusBarColor = getColor(R.color.start)

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
                (mainBinding.goBackFloatButton.layoutParams as FrameLayout.LayoutParams)
                    .bottomMargin = top
                insets
            }
        }

        //组件全部加载完成后，显示界面
        mainBinding.mainActivity.visibility = View.INVISIBLE
        viewModel.loadedComponentAmounts.observe(this) {
            if (it == COMPONENT_AMOUNTS) {
                window.statusBarColor = getColor(ThemeUtil.getToolBarColor(this))
                mainBinding.mainActivity.visibility = View.VISIBLE
                viewModel.isActivityFirstStart = false
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
            val date = it.toDate()
            LogUtil.d(this, "日期发生变化 $date")
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

        viewModel.getCharacterByMonth(systemDate.month) //首次启动刷新当前月的生日角色

        viewModel.birthdayCard.observe(this) {
            //初次启动初始化
            if (viewModel.isActivityFirstStart) {
                birCardInit(it, mainBinding)
                viewModel.componentLoadCompleted()
                return@observe
            }
            //刷新生日卡片
            when (it) {
                0 -> {
                    if (viewModel.birCardStatus == View.VISIBLE){
                        viewModel.birCardStatus = View.INVISIBLE
                        runBirthdayCardAnim(mainBinding, false)
                    }
                }
                else -> {
                    refreshBirthdayCard(it, mainBinding)
                    if (viewModel.birCardStatus == View.INVISIBLE) {
                        viewModel.birCardStatus = View.VISIBLE
                        runBirthdayCardAnim(mainBinding, true)
                    }
                }
            }
        }

        //设置滑动监听器，实现生日卡片的折叠/展开
        mainBinding.mainView.setOnTouchListener { _, event ->
            handleMainViewTouchEvent(event)
            true
        }

        //dailyTag服务
        viewModel.dailyTag.observe(this) {
            refreshDailyTag(mainBinding, it)
            viewModel.componentLoadCompleted()
        }

        viewModel.todayEvent.observe(this) {
            viewModel.run {
                it?.let { event ->
                    LogUtil.d("Event", "本期活动序号为：${event.id}")
                    //如果activity意外重启，若当前活动不为当日活动，则不刷新活动状态（说明不是初次启动）
                    if (isActivityFirstStart) {
                        eventStartTime = EventUtil.getEventStartTime(event)
                        if (event.endDate != null) {
                            eventEndTime = EventUtil.getEventEndTime(IntDate(event.endDate!!))
                            EventUtil.setEventLength(eventEndTime!! - eventStartTime!!)
                        } else {
                            eventEndTime = EventUtil.getEventEndTime(event)
                        }
                        refreshEventComponent(event, mainBinding) //初次启动刷新活动状态
                        viewModel.componentLoadCompleted()
                    }
                    //无论是否初次启动，都需要加入观察者
                    addEventObserver(mainBinding)
                }
                getDailyTag() //初次启动刷新DailyTag
            }
        }
        viewModel.getTodayEvent() //获取当天活动

        //返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            goBackToSystemDate(mainBinding)
        }

        //其他activity的跳转请求
        viewModel.jumpDate.observe(this) {
            jumpDate(mainBinding.viewPager, CalendarUtil(it))
        }

        //切换主题
        lifecycleScope.launch {
            //重启activity会导致observer重新注册，由于stateflow replay=1，会导致重复recreate，因此需要加入判定
            viewModel.activityRecreate.collect {
                if (!viewModel.isActivityRecreated) {
                    recreate()
                    viewModel.isActivityRecreated = true
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> startActivity<SettingsActivity>()
            R.id.menu_jump -> chooseDate(findViewById(R.id.viewPager))
            R.id.app_bar_search -> startActivity<SearchActivity>()
        }
        return true
    }

    private fun addEventObserver(mainBinding: ActivityMainBinding) {
        //观察活动变化，刷新活动组件内容
        viewModel.event.observe(this) {
            val currentDate = viewModel.currentDate.value!!.toDate()
            //活动小于第一期或者大于最后一期时，隐藏活动卡片
            if (it == null || currentDate - IntDate(it.startDate) >= 13) {
                LogUtil.d("Event", "currentDate $currentDate startDate ${it?.startDate}")
                mainBinding.eventCard.eventCardItem.visibility = View.GONE
            } else {
                mainBinding.eventCard.eventCardItem.visibility = View.VISIBLE
                LogUtil.d("Event", "Event id is ${it.id}")
                //相同活动之间移动，不刷新活动
                if (!EventUtil.isSameEvent(mainBinding.eventCard.eventType.text.toString(),
                        it.id.toInt())) {
                    refreshEventComponent(it, mainBinding)
                }
            }
        }
    }

    //刷新dailyTag
    private fun refreshDailyTag(binding: ActivityMainBinding, dailyTag: DailyTag) {
        //用户偏好不存在，不启动dailytag
        if (dailyTag.preferenceNearlyBandEvent == null && dailyTag.preferenceCharacter == null) {
            binding.dailytagCard.cardView.visibility = View.GONE
            return
        }
        //刷新标题
        binding.dailytagCard.dailytagTitle.text = StringBuilder().apply {
            append("${systemDate.getTimeName()}好")
            if (dailyTag.userName != "")
                append("，${dailyTag.userName}")
        }
        //刷新角色订阅
        dailyTag.preferenceCharacter?.let { character->
            binding.dailytagCard.dailytagCardBirthday.run {
                //角色头像和名字
                Glide.with(this@MainActivity)
                    .load(EventUtil.matchCharacter(character.id.toInt())).into(charImage)
                charImage.setOnClickListener {
                    startCharacterListActivity(character.id.toInt())
                }
                Glide.with(this@MainActivity)
                    .load(CharacterUtil.matchCharacter(character.id.toInt())).into(charNameImage)
                charNameImage.setOnClickListener {
                    startCharacterListActivity(character.id.toInt())
                }
                //倒数日
                val birthdayAway = CharacterUtil.birthdayAway(character.birthday, systemDate)
                birthdayCountdown.text = birthdayAway.toString()
                birthdayCountdown.setOnClickListener {
                    val target = CalendarUtil(CharacterUtil
                        .getNextBirthdayDate(character.birthday, systemDate))
                    //防止重复跳转
                    if (!target.isSameDate(viewModel.currentDate.value!!)) {
                        jumpDate(binding.viewPager, target)
                    }
                }
                //更新进度条
                birBar.setProgressCompat(
                    ((365 - birthdayAway) / 365.0 * 100).toInt(), true)
                birthdayView.visibility = View.VISIBLE
            }
        }
        if (dailyTag.preferenceCharacter == null)
            binding.dailytagCard.dailytagCardBirthday.birthdayView.visibility = View.GONE
        //刷新活动订阅
        dailyTag.preferenceNearlyBandEvent?.let {  event->
            binding.dailytagCard.dailytagCardEvent.run {
                //刷新活动属性
                Glide.with(this@MainActivity).load(EventUtil.matchAttrs(event.attrs))
                    .into(eventAttrs)
                //刷新乐队图片
                Glide.with(this@MainActivity).load(EventUtil.getBandPic(event))
                    .into(bandImage)
                bandImage.setOnClickListener {
                    startActivity<EventListActivity>(
                        "current_id" to event.id.toInt(),
                        "band_id" to EventUtil.getBand(event).id
                    )
                }
                //倒数日
                val eventAway = (IntDate(event.startDate) - systemDate.toDate())
                eventCountdown.text = eventAway.toString()
                eventCountdown.setOnClickListener {
                    val target = CalendarUtil(IntDate(event.startDate))
                    //防止重复跳转
                    if (!target.isSameDate(viewModel.currentDate.value!!)) {
                        jumpDate(binding.viewPager, target)
                    }
                }
                //更新进度条
                lifecycleScope.launch {
                    val lastEvent = viewModel.getBandLastEventByDate(
                        date = systemDate.toDate(),
                        character1Id = event.character1
                    )
                    //新乐队可能没有往期活动
                    val lastDate = lastEvent?.startDate ?: systemDate.toDate().value
                    val interval = IntDate(event.startDate) - IntDate(lastDate)
                    eventBar.setProgressCompat(
                        ((interval - eventAway) / interval.toDouble() * 100).toInt(), true)
                }
                eventView.visibility = View.VISIBLE
            }
        }
        if (dailyTag.preferenceNearlyBandEvent == null)
            binding.dailytagCard.dailytagCardEvent.eventView.visibility = View.GONE
        //用户偏好存在时，启动dailyTag
        binding.dailytagCard.cardView.visibility = View.VISIBLE
    }

    private fun birCardInit(id: Int, binding: ActivityMainBinding) {
        if (id > 0) {
            refreshBirthdayCard(id, binding)
            viewModel.birCardStatus = View.VISIBLE
        }
        else {
            val mainLinearLayout = binding.mainView
            val birCardIndex = mainLinearLayout.indexOfChild(binding.birCardParent)

            val cardHeight = binding.birCard.cardView.height.toFloat()
            val translationY = -cardHeight - GenericUtil.dpToPx(10)

            for (i in birCardIndex + 1 until mainLinearLayout.childCount) {
                val cardBelow = mainLinearLayout.getChildAt(i)
                cardBelow.translationY = translationY
            }
            binding.birCard.cardView.translationY = translationY
            viewModel.birCardStatus = View.INVISIBLE
        }
    }

    //刷新生日卡片
    private fun refreshBirthdayCard(id: Int, binding: ActivityMainBinding) {
        if (id == 12 || id == 17) {
            Glide.with(this).load(CharacterUtil.matchCharacter(12))
                .into(binding.birCard.birChar1)
            Glide.with(this).load(CharacterUtil.matchCharacter(17))
                .into(binding.birCard.birChar2)
            binding.birCard.birChar1.setOnClickListener {
                startCharacterListActivity(12)
            }
            binding.birCard.birChar2.setOnClickListener {
                startCharacterListActivity(17)
            }
            binding.birCard.birChar2.visibility = View.VISIBLE
        } else {
            binding.birCard.birChar2.visibility = View.GONE
            Glide.with(this).load(CharacterUtil.matchCharacter(id))
                .into(binding.birCard.birChar1)
            binding.birCard.birChar1.setOnClickListener {
                startCharacterListActivity(id)
            }
            binding.birCard.birChar2.setOnClickListener {
                startCharacterListActivity(id)
            }
        }
    }

    //处理生日卡片折叠/展开动画时的手势
    private fun handleMainViewTouchEvent(event: MotionEvent) {
        //生日卡片不显示时不处理
        if (viewModel.birthdayCard.value!! < 1 && viewModel.currentDateBirthdayCard < 1)
            return
        //修改当前日期的生日卡片角色ID
        if (viewModel.birthdayCard.value!! > 0)
            viewModel.currentDateBirthdayCard = viewModel.birthdayCard.value!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                viewModel.touchEventStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - viewModel.touchEventStartY
                if (deltaY > 0) {
                    //向下滑动，触发展开动画
                    viewModel.refreshBirthdayCard(viewModel.currentDateBirthdayCard)
                } else if (deltaY < 0) {
                    //向上滑动，触发折叠动画
                    viewModel.refreshBirthdayCard(0)
                }
            }
        }
    }

    private fun runBirthdayCardAnim(binding: ActivityMainBinding, isInsert: Boolean) {
        val mainLinearLayout = binding.mainView
        val birCardIndex = mainLinearLayout.indexOfChild(binding.birCardParent)
        val animDuration: Long = 450
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

    private fun refreshEventComponent(event: Event, binding: ActivityMainBinding) {
        LogUtil.d(this, "刷新活动组件")
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
        event.character4?.let { character4 ->
            binding.eventCard.char4.setOnClickListener {
                startCharacterListActivity(character4)
            }
        }
        Glide.with(this).load(EventUtil.matchCharacter(event.character5))
            .into(binding.eventCard.char5)
        event.character5?.let { character5 ->
            binding.eventCard.char5.setOnClickListener {
                startCharacterListActivity(character5)
            }
        }
        //刷新活动属性
        Glide.with(this).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.eventCard.eventAttrs)
        //刷新乐队图片
        Glide.with(this).load(EventUtil.getBandPic(event))
            .into(binding.eventCard.eventBand)
        binding.eventCard.eventBand.setOnClickListener {
            startActivity<EventListActivity>(
                "current_id" to event.id.toInt(),
                "band_id" to EventUtil.getBand(event).id
            )
        }
        //刷新活动图片
        val eventId = EventUtil.eventIdFormat(event.id.toInt())
        lifecycleScope.launch {
            viewModel.getEventPic(eventId) {
                binding.eventCard.eventBackground.background = it
            }
        }
        binding.eventCard.eventButton.setOnClickListener {
            startActivity<EventListActivity>("current_id" to event.id.toInt())
        }
    }

    private fun refreshEventStatus(event: Event, binding: ActivityMainBinding) {
        val todayEventId = viewModel.todayEvent.value?.id
        val eventId = event.id
        todayEventId?.let {
            log(this, "刷新活动状态")
            when {
                eventId < todayEventId -> {
                    binding.eventCard.eventProgressName.setText(R.string.finish)
                    binding.eventCard.eventProgress.progress = 100
                }
                eventId == todayEventId -> {
                    val systemTime = systemDate.getTimeInMillis()
                    val eventStartTime = viewModel.eventStartTime
                    val eventEndTime = viewModel.eventEndTime
                    if (eventStartTime != null && eventEndTime != null) {
                        when {
                            systemTime < eventStartTime -> {
                                binding.eventCard.eventProgressName.setText(R.string.prepare)
                                binding.eventCard.eventProgress.progress = 0
                            }
                            systemTime >= eventEndTime -> {
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
        val dialog = AlertDialog.Builder(this)
            .setTitle("请选择跳转的日期")
            .setIcon(R.mipmap.ic_launcher)
            .setView(view)
            .setNegativeButton("取消") { _, _ ->
            }
            .setPositiveButton("确认") { _, _ ->
                val calendarUtil = CalendarUtil().apply {
                    year = datePicker.year
                    month = datePicker.month + 1
                    day = datePicker.dayOfMonth
                }
                if (!calendarUtil.isSameDate(viewModel.currentDate.value!!)) //原地选择不跳转
                    jumpDate(viewPager, calendarUtil)
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
        startActivity<CharacterListActivity>("current_id" to id)
    }

}