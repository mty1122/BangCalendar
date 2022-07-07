package com.mty.bangcalendar.ui.guide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.settings.SettingsActivity
import com.mty.bangcalendar.util.ThemeUtil

class GuideActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(GuideViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        supportActionBar?.hide()
        appStartInit()
    }

    private fun appStartInit() {
        viewModel.initData.observe(this) { initData ->
            ThemeUtil.setCurrentTheme(initData.theme)
            if (initData.isFirstStart) {
                LogUtil.d("AppInit", "App is first start")
                viewModel.refreshDataProgress.observe(this) { progress ->
                    if (progress == 100) {
                        startMainActivity()
                    }
                }
                //初始化数据库
                viewModel.initDataBase(this)
            } else {
                LogUtil.d("AppInit", "App is not first start")
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