package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.main.adapter.CalendarViewAdapter
import com.mty.bangcalendar.ui.main.adapter.CalendarViewPagerAdapter
import com.mty.bangcalendar.ui.main.view.BirthdayCardView
import com.mty.bangcalendar.ui.main.view.DailyTagView
import com.mty.bangcalendar.ui.main.view.EventCardView
import com.mty.bangcalendar.ui.main.view.MainViewInitializer
import com.mty.bangcalendar.ui.search.SearchActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    private val dailyTagView by lazy { DailyTagView() }
    private val eventCardView by lazy { EventCardView() }
    private val birthdayCardView by lazy { BirthdayCardView() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //初次启动时，状态栏颜色与引导界面一致
        if (viewModel.mainUiState.value.isFirstStart)
            window.statusBarColor = getColor(R.color.start)

        //小白条沉浸
        navigationBarImmersion(mainBinding)

        //开始加载界面
        viewModel.startLoading()

        //组件全部加载完成后，显示界面
        //由于组件是异步加载的，可能属性的修改发生在onCreate后，会出现抽搐的情况
        //为保证所有组件全部加载完成，再统一显示给用户
        mainBinding.mainActivity.visibility = View.INVISIBLE
        lifecycleScope.launch {
            viewModel.mainUiState
                .map { it.isLoading }
                .distinctUntilChanged()
                .collect { isLoading->
                    if (!isLoading) {
                        window.statusBarColor =
                            getColor(ThemeUtil.getToolBarColor(this@MainActivity))
                        mainBinding.mainActivity.visibility = View.VISIBLE
                    }
                }
        }
        //获取初始化数据
        lifecycleScope.launch {
            viewModel.fetchInitData().collect{ initData->
                //初始化界面
                MainViewInitializer(this@MainActivity, mainBinding, viewModel, initData,
                    viewModel.mainUiState.value, dailyTagView, eventCardView, birthdayCardView)
                    .initViews()
                viewModel.loadCompleted()
            }
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

        //为三个卡片组件设置观察者，当ui状态改变时，更新界面
        setEventCardUiStateObserver(mainBinding)
        setBirthdayCardUiStateObserver(mainBinding)
        setDailyTagUiStateObserver(mainBinding)

        //设置滑动监听器，实现生日卡片的折叠/展开
        mainBinding.mainView.setOnTouchListener { _, event ->
            birthdayCardView.handleMainViewTouchEvent(
                event,
                mainBinding,
                { viewModel.birthdayCardUiState.value!! },
                { viewModel.isBirthdayCardVisible },
                { viewModel.isBirthdayCardVisible = it }
            )
            true
        }

        //配置返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            jumpDate(mainBinding.viewPager, systemDate)
        }

        //监听其他activity的跳转请求
        viewModel.jumpDate.observe(this) {
            val target = CalendarUtil(it)
            //防止重复跳转
            if (!target.isSameDate(viewModel.currentDate.value!!)) {
                jumpDate(mainBinding.viewPager, target)
            }
        }

        //应用主题发生改变时，重启界面
        lifecycleScope.launch {
            viewModel.mainUiState
                .map { it.shouldRecreate }
                .distinctUntilChanged()
                .collect { shouldRecreate->
                    if (shouldRecreate) {
                        recreate()
                        viewModel.recreateActivityCompleted()
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

    private fun navigationBarImmersion(binding: ActivityMainBinding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            binding.mainActivity.setOnApplyWindowInsetsListener { view, insets ->
                val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.updatePadding(top = top)
                (binding.goBackFloatButton.layoutParams as FrameLayout.LayoutParams)
                    .bottomMargin = top
                insets
            }
        }
    }

    private fun setBirthdayCardUiStateObserver(mainBinding: ActivityMainBinding) {
        viewModel.birthdayCardUiState.observe(this) {
            //加载中不刷新生日卡片
            if (viewModel.mainUiState.value.isLoading)
                return@observe
            //刷新生日卡片
            when (it) {
                0 -> {
                    if (viewModel.isBirthdayCardVisible){
                        viewModel.isBirthdayCardVisible = false
                        birthdayCardView.runBirthdayCardAnim(mainBinding, false)
                    }
                }
                else -> {
                    birthdayCardView.refreshBirthdayCard(this, it, mainBinding)
                    if (!viewModel.isBirthdayCardVisible) {
                        viewModel.isBirthdayCardVisible = true
                        birthdayCardView.runBirthdayCardAnim(mainBinding, true)
                    }
                }
            }
        }
    }

    private fun setDailyTagUiStateObserver(mainBinding: ActivityMainBinding) {
        viewModel.dailyTagUiState.observe(this) {
            dailyTagView.refreshDailyTag(this, viewModel, mainBinding, it)
        }
    }

    private fun setEventCardUiStateObserver(mainBinding: ActivityMainBinding) {
        //观察活动变化，刷新活动组件内容
        viewModel.eventCardUiState.observe(this) {
            //加载中不刷新活动卡片
            if (viewModel.mainUiState.value.isLoading)
                return@observe
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
                    eventCardView.refreshEventComponent(this, lifecycleScope,
                        viewModel.mainUiState.value, it.event, it.eventPicture!!, mainBinding)
                    //启动显示动画
                    eventCardView.runEventCardAnim(mainBinding, 1f)
                //不同活动之间移动，刷新活动
                } else if (!EventUtil.isSameEvent(mainBinding.eventCard.eventType.text.toString(),
                        it.event.id.toInt())) {
                    eventCardView.refreshEventComponent(this, lifecycleScope,
                        viewModel.mainUiState.value,it.event, it.eventPicture!!, mainBinding)
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

}