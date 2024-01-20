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
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.main.view.BirthdayCardView
import com.mty.bangcalendar.ui.main.view.CalendarView
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

    private val calendarView by lazy { CalendarView() }
    private val dailyTagView by lazy { DailyTagView() }
    private val eventCardView by lazy { EventCardView() }
    private val birthdayCardView by lazy { BirthdayCardView() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //膨胀布局，配置viewBinding
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //配置toolBar
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
                    viewModel.mainUiState.value, calendarView, dailyTagView, eventCardView,
                    birthdayCardView)
                    .initViews()
                //重置jumpDate的值，防止activity重启后，旧的跳转日期被再次监听到
                if (!viewModel.mainUiState.value.isFirstStart)
                    viewModel.setJumpDate(viewModel.currentDate.value!!)
                //初始化完成
                viewModel.loadCompleted()
            }
        }

        //观察当前日期变化，刷新顶部日期、返回今天按钮、活动组件和生日组件
        viewModel.currentDate.observe(this) { date->
            log(this, "日期发生变化 $date")
            viewModel.getEventByDate(date) //刷新活动
            viewModel.refreshBirthdayCard(date) //刷新生日卡片
            //顶部日期刷新
            mainBinding.date.text = StringBuilder().run {
                append(date.getYear())
                append("年")
                append(date.getMonth())
                append("月")
            }
            //返回今天浮窗
            if (date.getYear() == systemDate.year && date.getMonth() == systemDate.month
                && date.getDay()== systemDate.day) {
                mainBinding.goBackFloatButton.visibility = View.GONE
            } else {
                mainBinding.goBackFloatButton.visibility = View.VISIBLE
            }
        }

        //为三个卡片组件设置观察者，当ui状态改变时，更新界面
        setEventCardUiStateObserver(mainBinding)
        setBirthdayCardUiStateObserver(mainBinding)
        setDailyTagUiStateObserver(mainBinding)

        //设置滑动监听器，实现生日卡片的折叠/展开
        mainBinding.mainView.setOnTouchListener { _, event ->
            birthdayCardView.handleMainViewTouchEvent(
                event,
                mainBinding,
            ) { viewModel.birthdayCardUiState.value!! }
            true
        }

        //配置返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            calendarView.jumpDate(mainBinding.viewPager, systemDate, lifecycleScope,
                { viewModel.refreshCurrentDate(it) },
                { viewModel.fetchCharacterByMonth(it) })
        }

        //监听其他activity的跳转请求
        viewModel.jumpDate.observe(this) { targetDate->
            val target = CalendarUtil(targetDate)
            //防止重复跳转 && 防止activity重启后replay引起的跳转
            if (target.toDate().value != viewModel.currentDate.value!!.value &&
                !viewModel.mainUiState.value.isLoading) {
                calendarView.jumpDate(mainBinding.viewPager, target, lifecycleScope,
                    { viewModel.refreshCurrentDate(it) },
                    { viewModel.fetchCharacterByMonth(it) })
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
            birthdayCardView.handleUiState(this, mainBinding, it)
        }
    }

    private fun setDailyTagUiStateObserver(mainBinding: ActivityMainBinding) {
        viewModel.dailyTagUiState.observe(this) {
            dailyTagView.refreshDailyTag(this, viewModel, mainBinding, it)
        }
    }

    private fun setEventCardUiStateObserver(mainBinding: ActivityMainBinding) {
        viewModel.eventCardUiState.observe(this) {
            //加载中不刷新活动卡片
            if (viewModel.mainUiState.value.isLoading)
                return@observe
            //刷新活动卡片
            eventCardView.handleUiState(this, lifecycleScope,
                viewModel.currentDate.value!!,
                viewModel.mainUiState.value, it, mainBinding)
        }
    }

    //用于弹出跳转日期相关界面
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
                //原地选择不跳转
                if (calendarUtil.toDate().value != viewModel.currentDate.value!!.value)
                    calendarView.jumpDate(viewPager, calendarUtil, lifecycleScope,
                        { viewModel.refreshCurrentDate(it) },
                        { viewModel.fetchCharacterByMonth(it) })
            }
            .create()
        dialog.show()
    }

}