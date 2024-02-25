package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavBarImmersive
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityMainBinding
import com.mty.bangcalendar.logic.model.MainViewInitData
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.main.state.MainUiState
import com.mty.bangcalendar.ui.main.view.BirthdayCardView
import com.mty.bangcalendar.ui.main.view.CalendarView
import com.mty.bangcalendar.ui.main.view.DailyTagView
import com.mty.bangcalendar.ui.main.view.EventCardView
import com.mty.bangcalendar.ui.search.SearchActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mainBinding: ActivityMainBinding

    @Inject lateinit var calendarView: CalendarView
    @Inject lateinit var dailyTagView: DailyTagView
    @Inject lateinit var eventCardView: EventCardView
    @Inject lateinit var birthdayCardView: BirthdayCardView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //初次启动时，状态栏和导航栏颜色与引导界面一致
        if (viewModel.mainUiState.value.isFirstStart) {
            window.statusBarColor = getColor(R.color.start)
            window.navigationBarColor = getColor(R.color.start)
        }

        //膨胀布局，配置viewBinding
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //配置toolBar
        setSupportActionBar(mainBinding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
                        window.statusBarColor = ThemeUtil.getToolBarColor(this@MainActivity)
                        if (!isNavBarImmersive)
                            window.navigationBarColor =
                                ThemeUtil.getBackgroundColor(this@MainActivity)
                        mainBinding.mainActivity.visibility = View.VISIBLE
                    }
                }
        }
        //获取初始化数据
        lifecycleScope.launch {
            viewModel.fetchInitData().collect{ initData->
                //初始化界面
                initViews(viewModel.mainUiState.value, initData)
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
        setEventCardUiStateObserver()
        setBirthdayCardUiStateObserver()
        setDailyTagUiStateObserver()

        //设置滑动监听器，实现生日卡片的折叠/展开
        mainBinding.mainView.setOnTouchListener { _, event ->
            birthdayCardView.handleMainViewTouchEvent(
                event,
                mainBinding,
                viewModel.birthdayCardUiState.value!!
            )
            true
        }

        //配置返回今天按钮
        mainBinding.goBackFloatButton.setOnClickListener {
            lifecycleScope.launch {
                calendarView.jumpDate(mainBinding.viewPager, systemDate,
                    onDateChange = { viewModel.refreshCurrentDate(it) },
                    fetchBirthdayMapByMonth = { viewModel.fetchBirthdayMapByMonth(it) })
            }
        }

        //监听其他activity的跳转请求
        viewModel.jumpDate.observe(this) { targetDate->
            val target = CalendarUtil(targetDate)
            //防止重复跳转 && 防止activity重启后replay引起的跳转
            if (target.toDate().value != viewModel.currentDate.value!!.value &&
                !viewModel.mainUiState.value.isLoading) {
                lifecycleScope.launch{
                    calendarView.jumpDate(mainBinding.viewPager, target,
                        { viewModel.refreshCurrentDate(it) },
                        { viewModel.fetchBirthdayMapByMonth(it) })
                }
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

    override fun navBarImmersion(rootView: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        //关闭装饰窗口自适应
        window.setDecorFitsSystemWindows(false)
        rootView.setOnApplyWindowInsetsListener { view, insets ->
            val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                .getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottom = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            //手动设置根视图到顶部的距离（状态栏高度）
            view.updatePadding(top = top)
            //手动设置悬浮按钮到底部的距离（导航栏高度+原本的margin）
            mainBinding.goBackFloatButton.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    (mainBinding.goBackFloatButton.layoutParams as FrameLayout.LayoutParams)
                        .bottomMargin += bottom
                    mainBinding.birCard.cardView.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                }
            })
            insets
        }
        //设置导航栏透明
        window.navigationBarColor = getColor(R.color.transparent)
    }

    private fun setBirthdayCardUiStateObserver() {
        viewModel.birthdayCardUiState.observe(this) {
            //加载中不刷新生日卡片
            if (viewModel.mainUiState.value.isLoading)
                return@observe
            //刷新生日卡片
            birthdayCardView.handleUiState(mainBinding, it)
        }
    }

    private fun setDailyTagUiStateObserver() {
        viewModel.dailyTagUiState.observe(this) { uiState->
            dailyTagView.refreshDailyTag(mainBinding.dailytagCard, uiState) {
                viewModel.setJumpDate(it)
            }
        }
    }

    private fun setEventCardUiStateObserver() {
        viewModel.eventCardUiState.observe(this) {
            //加载中不刷新活动卡片
            if (viewModel.mainUiState.value.isLoading)
                return@observe
            //刷新活动卡片
            lifecycleScope.launch {
                eventCardView.handleUiState(viewModel.currentDate.value!!,
                    viewModel.mainUiState.value, it, mainBinding.eventCard)
            }
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
                    lifecycleScope.launch {
                        calendarView.jumpDate(viewPager, calendarUtil,
                            onDateChange = { viewModel.refreshCurrentDate(it) },
                            fetchBirthdayMapByMonth = { viewModel.fetchBirthdayMapByMonth(it) })
                    }
            }
            .create()
        dialog.show()
    }

    private suspend fun initViews(
        mainUiState: MainUiState, initData: MainViewInitData) = coroutineScope {
        /* 下方为四大组件初始化（日历、dailyTag、生日卡片、活动卡片） */
        //加载日历模块，如果不是初次启动，使用viewModel保存的状态
        launch {
            val initDate = if (mainUiState.isFirstStart) null else viewModel.currentDate.value!!
            calendarView.calendarInit(
                mainBinding.viewPager, initDate,
                onDateChange = { viewModel.refreshCurrentDate(it) },
                getCurrentDate = { viewModel.currentDate.value!! },
                fetchBirthdayMapByMonth = { viewModel.fetchBirthdayMapByMonth(it) }
            )
        }
        //加载DailyTag
        val dailyTagDeferred = async {
            initData.dailyTagUiState.collect { uiState->
                dailyTagView.refreshDailyTag(mainBinding.dailytagCard, uiState) {
                    viewModel.setJumpDate(it)
                }
            }
        }
        //加载活动卡片，如果不是初次启动，使用viewModel保存的状态
        val eventCardDeferred = async {
            val  currentDate =
                if (mainUiState.isFirstStart) systemDate.toDate()
                else viewModel.currentDate.value!!
            eventCardView.eventCardInit(mainBinding.eventCard, mainUiState, initData.currentEvent,
                currentDate, initData.eventPicture)
        }
        //加载生日卡片
        val birthdayCardDeferred = async {
            initData.birthdayCardUiState.collect{ birthdayCardUiState ->
                //等待其膨胀完成后再初始化，防止获取不到高度
                suspendCoroutine {
                    mainBinding.birCard.cardView.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            birthdayCardView.birCardInit(birthdayCardUiState, mainBinding)
                            it.resume(Unit)
                            mainBinding.birCard.cardView.viewTreeObserver
                                .removeOnGlobalLayoutListener(this)
                        }
                    })
                }
            }
        }
        //等待加载完成
        dailyTagDeferred.await()
        eventCardDeferred.await()
        birthdayCardDeferred.await()
    }

}