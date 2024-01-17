package com.mty.bangcalendar.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.list.CharacterListActivity
import com.mty.bangcalendar.ui.list.EventListActivity
import com.mty.bangcalendar.ui.main.adapter.CalendarViewAdapter
import com.mty.bangcalendar.ui.main.adapter.CalendarViewPagerAdapter
import com.mty.bangcalendar.ui.main.view.DailyTagView
import com.mty.bangcalendar.ui.main.view.EventCardView
import com.mty.bangcalendar.ui.main.view.MainViewInitializer
import com.mty.bangcalendar.ui.search.SearchActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    companion object {
        const val COMPONENT_AMOUNTS = 3 //主界面组件的数量
    }

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    //记录Activity是否创建（用于设置生日卡片的位置）
    private var isActivityCreated = false
    //记录滑动手势的起始点，用于折叠卡片
    private var touchEventStartY = 0f

    private val dailyTagView by lazy { DailyTagView() }
    private val eventCardView by lazy { EventCardView() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        isActivityCreated = false
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
        //由于组件是异步加载的，可能属性的修改发生在onCreate后，会出现抽搐的情况
        //为保证所有组件全部加载完成，再统一显示给用户，故设置此状态
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

        viewModel.birthdayCardUiState.observe(this) {
            //生日卡片初始化
            if (!isActivityCreated) {
                //等待其膨胀完成后再初始化，防止获取不到高度
                mainBinding.birCard.cardView.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        birCardInit(it, mainBinding)
                        isActivityCreated = true
                        viewModel.componentLoadCompleted()
                        mainBinding.birCard.cardView.viewTreeObserver
                            .removeOnGlobalLayoutListener(this)
                    }
                })
                return@observe
            }
            //刷新生日卡片
            when (it) {
                0 -> {
                    if (viewModel.isBirthdayCardVisible){
                        viewModel.isBirthdayCardVisible = false
                        runBirthdayCardAnim(mainBinding, false)
                    }
                }
                else -> {
                    refreshBirthdayCard(it, mainBinding)
                    if (!viewModel.isBirthdayCardVisible) {
                        viewModel.isBirthdayCardVisible = true
                        runBirthdayCardAnim(mainBinding, true)
                    }
                }
            }
        }

        //设置滑动监听器，实现生日卡片的折叠/展开
        mainBinding.mainView.setOnTouchListener { _, event ->
            handleMainViewTouchEvent(event, mainBinding)
            true
        }

        //dailyTag服务
        viewModel.dailyTagUiState.observe(this) {
            dailyTagView.refreshDailyTag(this, viewModel, mainBinding, it)
        }

        //这里的todayEvent是指开始日期小于等于今日最新的活动，所以一定存在
        lifecycleScope.launch {
            viewModel.fetchInitData().collect{ initData->
                //初始化界面
                MainViewInitializer(this@MainActivity, mainBinding,
                    viewModel, initData, dailyTagView, eventCardView).initViews()
                initData.todayEvent.let { event->
                    LogUtil.d("Event", "本期活动序号为：${event.id}")
                    //如果activity意外重启，若当前活动不为当日活动，则不刷新活动状态（说明不是初次启动）
                    if (viewModel.isActivityFirstStart) {
                        viewModel.eventStartTime = EventUtil.getEventStartTime(event)
                        if (event.endDate != null) {
                            viewModel.eventEndTime =
                                EventUtil.getEventEndTime(IntDate(event.endDate!!))
                            EventUtil.setEventLength(viewModel.eventEndTime!! -
                                    viewModel.eventStartTime!!)
                        } else {
                            viewModel.eventEndTime = EventUtil.getEventEndTime(event)
                        }
                        eventCardInit(mainBinding, event, initData.todayEventPicture)
                        viewModel.componentLoadCompleted()
                    }
                    //无论是否初次启动，都需要加入观察者
                    addEventObserver(mainBinding)
                }
            }
        }

        //返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            goBackToSystemDate(mainBinding)
        }

        //其他activity的跳转请求
        viewModel.jumpDate.observe(this) {
            val target = CalendarUtil(it)
            //防止重复跳转
            if (!target.isSameDate(viewModel.currentDate.value!!)) {
                jumpDate(mainBinding.viewPager, target)
            }
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
        viewModel.eventCardUiState.observe(this) {
            val currentDate = viewModel.currentDate.value!!.toDate()
            //活动小于第一期或者大于最后一期的情况
            if (it.event == null || currentDate - IntDate(it.event.startDate) >= 13) {
                if (viewModel.isEventCardVisible) {
                    viewModel.isEventCardVisible = false
                    //启动隐藏动画
                    eventCardView.runEventCardAnim(mainBinding, 0f)
                    //取消注册监听器
                    eventCardView.cancelListener(mainBinding)
                }
            //活动合法的情况
            } else {
                LogUtil.d("Event", "Event id is ${it.event.id}")
                //不可见时，刷新活动
                if (!viewModel.isEventCardVisible) {
                    viewModel.isEventCardVisible = true
                    eventCardView.refreshEventComponent(this, lifecycleScope, it.event,
                        it.eventPicture!!, mainBinding)
                    //启动显示动画
                    eventCardView.runEventCardAnim(mainBinding, 1f)
                //不同活动之间移动，刷新活动
                } else if (!EventUtil.isSameEvent(mainBinding.eventCard.eventType.text.toString(),
                        it.event.id.toInt())) {
                    eventCardView.refreshEventComponent(this, lifecycleScope, it.event,
                        it.eventPicture!!, mainBinding)
                }
            }
        }
    }

    //生日卡片初始化
    private fun birCardInit(id: Int, binding: ActivityMainBinding) {
        if (id > 0) {
            refreshBirthdayCard(id, binding)
            viewModel.isBirthdayCardVisible = true
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
            viewModel.isBirthdayCardVisible = false
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
    private fun handleMainViewTouchEvent(event: MotionEvent, binding: ActivityMainBinding) {
        //生日卡片不显示时不处理
        if (viewModel.birthdayCardUiState.value!! < 1)
            return
        //处理滑动手势
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchEventStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - touchEventStartY
                //向下滑动，触发展开动画
                if (deltaY > 0 && !viewModel.isBirthdayCardVisible) {
                    viewModel.isBirthdayCardVisible = true
                    runBirthdayCardAnim(binding, true)
                //向上滑动，触发折叠动画
                } else if (deltaY < 0 && viewModel.isBirthdayCardVisible) {
                    viewModel.isBirthdayCardVisible = false
                    runBirthdayCardAnim(binding, false)
                }
            }
        }
    }

    private fun runBirthdayCardAnim(binding: ActivityMainBinding, isInsert: Boolean) {
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
    fun jumpDate(viewPager: ViewPager, target: CalendarUtil) {
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
            //如果不是同月跳转，则清空角色生日，防止闪烁
            if (viewModel.currentDate.value!!.year != target.year
                || viewModel.currentDate.value!!.month != target.month)
                viewAdapter.birthdayMap.clear()
            //刷新日历
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

    fun startCharacterListActivity(id: Int) {
        startActivity<CharacterListActivity>("current_id" to id)
    }

}