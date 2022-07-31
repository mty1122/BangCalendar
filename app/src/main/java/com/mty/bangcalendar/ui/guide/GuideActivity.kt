package com.mty.bangcalendar.ui.guide

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.ThemeUtil

class GuideActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(GuideViewModel::class.java) }

    private val eventConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val refreshBinder = p1 as EventRefreshService.RefreshBinder
            refreshBinder
                .refresh(findViewById(R.id.refreshProgress), findViewById(R.id.refreshText))
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    private val characterConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val refreshBinder = p1 as CharacterRefreshService.RefreshBinder
            refreshBinder
                .refresh(findViewById(R.id.refreshProgress), findViewById(R.id.refreshText))
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        supportActionBar?.hide()
        window.statusBarColor = getColor(R.color.start)
        appStartInit()
    }

    private fun appStartInit() {
        viewModel.initData.observe(this) { initData ->
            ThemeUtil.setCurrentTheme(initData.theme)
            if (initData.isFirstStart) {  /* 首次启动 */
                LogUtil.d("AppInit", "App is first start")
                findViewById<LinearLayout>(R.id.guideActivity).visibility = View.VISIBLE
                viewModel.refreshDataProgress.observe(this) { progress ->
                    when (progress) {
                        50 -> {
                            val intent =
                                Intent(this, CharacterRefreshService::class.java)
                            bindService(intent, characterConnection, Context.BIND_AUTO_CREATE)
                        }
                        100 -> {
                            unbindService(eventConnection)
                            unbindService(characterConnection)
                            val button = findViewById<Button>(R.id.refreshButton)
                            button.setOnClickListener {
                                startMainActivity()
                            }
                            findViewById<TextView>(R.id.refreshText)
                                .text = getString(R.string.init_complete)
                            button.isEnabled = true
                        }
                    }
                }
                val intent = Intent(this, EventRefreshService::class.java)
                bindService(intent, eventConnection, Context.BIND_AUTO_CREATE)
            } else if (systemDate.getDayOfWeak() == 2
                && initData.lastRefreshDay != systemDate.day) {  /* 每周一自动更新数据库 */
                val intent = Intent(this, EventRefreshService::class.java)
                startService(intent)
                startMainActivity()
            } else {
                LogUtil.d("AppInit", "App is not first start")
                LogUtil.d("AppInit", "Day of week ${systemDate.getDayOfWeak()}")
                startMainActivity()
            }
        }
        viewModel.getInitData()
    }

    private fun startMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        val isSettingsChange = intent.getBooleanExtra("settings_change", false)
        if (isSettingsChange) {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
        finish()
    }

}